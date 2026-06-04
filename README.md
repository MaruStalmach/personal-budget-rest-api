# Budget API

A REST API for personal budget management. It tracks **accounts** and the **income/expense transactions** assigned to them, keeps each account's balance up to date automatically, and produces per-account summaries (total income, total expenses, and a breakdown of expenses by category).

---

## Tech stack

- **Java 17**
- **Spring Boot 4** (Spring MVC, Spring Data JPA, Bean Validation)
- **PostgreSQL** for running the application
- **H2 (in-memory)** for the automated tests
- **Maven** (via the bundled Maven Wrapper — no local Maven install needed)
- **Docker / Docker Compose** for one-command startup

---

## Prerequisites

You only need different tools depending on what you want to do:

| I want to…                          | I need…                                                        |
| ----------------------------------- | -------------------------------------------------------------- |
| **Run the whole app** (recommended) | **Only Docker Desktop** — nothing else                         |
| Run the automated tests             | A **JDK 17+** (the project ships the Maven Wrapper)             |
| Run the app locally without Docker  | A **JDK 17+** and a reachable PostgreSQL instance              |

The recommended path is Docker: with Docker Desktop installed you do **not** need Java, Maven, or PostgreSQL on your machine — they all run inside containers. This is the most reliable way to run the project on any computer.

> Get Docker Desktop: https://www.docker.com/products/docker-desktop/ (Windows, macOS, and Linux; works on both Intel and Apple Silicon).

---

## Quick start with Docker (recommended)

With Docker Desktop **installed and running**:

```bash
git clone <your-repository-url>
cd budget-api
docker compose up --build
```

That single command:

1. builds the application image from source (first run downloads dependencies — give it a few minutes),
2. starts a PostgreSQL container and **automatically creates the `budget_db` database** (no manual `createdb` step),
3. waits until the database is healthy, then starts the app and creates the schema from the entities.

When you see a log line like `Started BudgetApiApplication`, the API is ready at:

```
http://localhost:8080
```

> If your Docker install is older, the command may be `docker-compose up --build` (with a hyphen) instead of `docker compose`.

### Verify it works

In another terminal:

```bash
# Create an account
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"name":"Main account"}'

# List accounts
curl http://localhost:8080/accounts
```

The first call should return `201 Created` with the new account, and the second `200 OK` with a list containing it.

### Stopping and resetting

```bash
# Stop (Ctrl+C if running in the foreground), then remove the containers:
docker compose down

# Run in the background instead:
docker compose up --build -d

# Follow the application logs:
docker compose logs -f app
```

> **Data is not persisted between `docker compose down` and the next `up`** — this is intentional, so every run starts from a clean, reproducible state. See *Persisting data* below if you want it to survive restarts.

---

## Running the tests

The test suite (unit + integration) runs against an **in-memory H2 database**, so **no PostgreSQL or Docker is required** to run it — just a JDK 17+.

```bash
# macOS / Linux
./mvnw test

# Windows
mvnw.cmd test
```

Useful variants:

```bash
./mvnw test -Dtest=AccountControllerIntegrationTest          # one class
./mvnw test -Dtest=AccountControllerIntegrationTest#createAccount_returns201  # one method
./mvnw test -Dtest='*IntegrationTest'                        # by pattern
```

The integration tests boot the full Spring context once (shared across tests) and exercise the real controller → service → repository → database stack; the unit tests cover service and entity logic in isolation with mocks.

---

## Running locally without Docker (optional, for development)

If you prefer to run the app directly on your machine, the simplest reliable setup is to start **only the database** in Docker and run the app with the Maven Wrapper:

```bash
# 1. Start just the PostgreSQL container (creates budget_db, listens on host port 5433)
docker compose up -d db

# 2. Run the app, pointing it at that database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/budget_db \
SPRING_DATASOURCE_USERNAME=budget \
SPRING_DATASOURCE_PASSWORD=budget \
./mvnw spring-boot:run
```

If you instead have your **own** local PostgreSQL, the app's built-in defaults expect a database `budget_db` on `localhost:5433` with user `postgres` / password `admin123` (see the configuration table below). Create that database first, or override the values with the environment variables above.

---

## Configuration

Connection settings are read from environment variables, falling back to local-development defaults when the variables are not set. This is what lets the same `application.properties` work both for a plain local run and inside Docker (where Compose supplies the variables).

| Environment variable           | Default (local)                                  | Value in Docker Compose                       |
| ------------------------------ | ------------------------------------------------ | --------------------------------------------- |
| `SPRING_DATASOURCE_URL`        | `jdbc:postgresql://localhost:5433/budget_db`     | `jdbc:postgresql://db:5432/budget_db`         |
| `SPRING_DATASOURCE_USERNAME`   | `postgres`                                       | `budget`                                      |
| `SPRING_DATASOURCE_PASSWORD`   | `admin123`                                       | `budget`                                      |
| `SPRING_JPA_HIBERNATE_DDL_AUTO`| `update`                                         | `update`                                      |

**About the two ports.** Inside Compose the app connects to `db:5432` (`db` is the service name on Docker's internal network, and the container always serves on its real port `5432`). The `5433:5432` mapping only matters if *you* want to connect to the database from your host machine (e.g. a SQL client at `localhost:5433`).

**About `ddl-auto=update`.** Hibernate creates/updates the schema from the entities on startup. This is convenient for a demo and means the project runs with zero manual schema setup. For a production system the recommended approach would be `validate` together with a migration tool such as Flyway or Liquibase.

> **Security note:** credentials are committed in plain text here only because this is a self-contained demo. A real deployment would supply secrets via the environment (exactly the `${VAR:default}` pattern used in `application.properties`), never in a committed file.

---

## API reference

Base URL: `http://localhost:8080`

### Accounts

| Method   | Path             | Description                                   | Success |
| -------- | ---------------- | --------------------------------------------- | ------- |
| `GET`    | `/accounts`      | List all accounts                             | 200     |
| `GET`    | `/accounts/{id}` | Get one account with its current balance      | 200     |
| `POST`   | `/accounts`      | Create an account (balance starts at 0)       | 201     |
| `DELETE` | `/accounts/{id}` | Delete an account (only if it has no transactions) | 204 |

### Transactions

| Method   | Path                 | Description                                            | Success |
| -------- | -------------------- | ----------------------------------------------------- | ------- |
| `GET`    | `/transactions`      | List transactions; optional `?from=`, `?to=`, `?category=` filters | 200 |
| `GET`    | `/transactions/{id}` | Get one transaction                                   | 200     |
| `POST`   | `/transactions`      | Add a transaction (the account balance updates automatically) | 201 |
| `DELETE` | `/transactions/{id}` | Delete a transaction (the balance is reverted)        | 204     |

### Summary

| Method | Path                       | Description                                          | Success |
| ------ | -------------------------- | ---------------------------------------------------- | ------- |
| `GET`  | `/accounts/{id}/summary`   | Total income, total expenses, and expenses by category | 200  |

### Status codes

| Code | Meaning                                                              |
| ---- | ------------------------------------------------------------------- |
| 200  | Successful read                                                     |
| 201  | Resource created                                                    |
| 204  | Resource deleted (no body returned)                                 |
| 400  | Invalid input (validation failure or non-positive transaction amount) |
| 404  | Account or transaction not found                                    |
| 409  | Conflict (e.g. deleting an account that still has transactions)     |

### Examples

```bash
# Create an account
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"name":"Main account"}'

# Add income to account 1
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"accountId":1,"amount":2500.00,"type":"INCOME","category":"Salary","description":"Monthly salary"}'

# Add an expense
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"accountId":1,"amount":40.00,"type":"EXPENSE","category":"Food"}'

# Filter transactions by category and date range
curl "http://localhost:8080/transactions?category=Food&from=2024-01-01T00:00:00&to=2026-12-31T23:59:59"

# Account summary
curl http://localhost:8080/accounts/1/summary

# Delete a transaction (balance reverts)
curl -X DELETE http://localhost:8080/transactions/1
```

> **Windows users:** the examples use Unix-style quoting. In PowerShell, prefer `curl.exe` with double quotes (escaping inner quotes), or use a GUI REST client such as Postman or the VS Code REST Client extension.

**Transaction fields:** `accountId` (required), `amount` (required, must be > 0), `type` (`INCOME` or `EXPENSE`, required), `category` (required), `description` (optional). The transaction time is set by the server.

---

## Project structure

```
src/
  main/
    java/com/budget/budget_api/
      controllers/      REST controllers
      services/         business logic
      entities/         JPA entities (Account, Transaction)
      repositories/     Spring Data repositories
      dtos/
        requests/       request payloads
        responses/      response payloads
      common/
        exception/      global handler, custom exceptions, error response
        types/          enums (e.g. TransactionType)
    resources/
      application.properties
  test/
    java/com/budget/budget_api/
      integration/      full-stack controller tests (MockMvc + H2)
      ...               unit tests for services and entities
    resources/
      application-test.properties   (H2 test profile)
Dockerfile
docker-compose.yml
.dockerignore
.gitattributes
pom.xml
mvnw, mvnw.cmd, .mvn/   Maven Wrapper
README.md
```

---

## Troubleshooting

| Symptom                                                        | Cause / fix                                                                                                   |
| -------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------- |
| `Cannot connect to the Docker daemon`                          | Docker Desktop isn't running — start it and retry.                                                            |
| `port is already allocated` / `address already in use` (8080)  | Something else uses port 8080. Change the app mapping in `docker-compose.yml` to e.g. `"8081:8080"`.          |
| Same error on `5433`                                           | Another PostgreSQL is using that host port. Change the db mapping to e.g. `"5434:5432"` (the app inside Compose is unaffected). |
| Build hangs or fails downloading dependencies                  | The first build needs internet access to pull base images and Maven dependencies. Check your connection/proxy. |
| `./mvnw: not found` or `bad interpreter` **inside the Docker build**, after cloning on Windows | Windows checkouts can convert the `mvnw` script to CRLF line endings, which breaks it in the Linux build. The included `.gitattributes` forces LF — make sure it's committed and re-clone. |
| App starts before the database is ready                        | Already handled: Compose waits for the DB healthcheck before starting the app.                                |
| Database is empty after a restart                              | Expected — data is not persisted by default (see below).                                                      |

### Persisting data (optional)

By default the database starts fresh on every `docker compose up`. To keep data across restarts, add a volume to the `db` service in `docker-compose.yml`:

```yaml
  db:
    # ...
    volumes:
      - pgdata:/var/lib/postgresql/data

volumes:
  pgdata:
```

To wipe a persisted volume and start clean again: `docker compose down -v`.

---

## Notes and possible improvements

- **Schema management:** swap `ddl-auto=update` for `validate` + Flyway/Liquibase migrations for versioned, production-safe schema changes.
- **API documentation:** add springdoc-openapi to expose interactive Swagger UI.
- **Category budgets / CSV export:** natural next features for the domain.