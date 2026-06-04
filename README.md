# Budget API

A REST API for personal budget management. It tracks **accounts** and the **income/expense transactions** assigned to them, keeps each account's balance up to date automatically, and produces per-account summaries (total income, total expenses, and a breakdown of expenses by category).

---

## Tech stack

- **Java 21**
- **Spring Boot 4** (Spring MVC, Spring Data JPA, Bean Validation)
- **PostgreSQL** for running the application
- **H2 (in-memory)** for the automated tests
- **Maven** (via the bundled Maven Wrapper — no local Maven install needed)
- **Docker / Docker Compose** for one-command startup

## Quick start with Docker 

With Docker Desktop **installed and running**:

```bash
git clone <your-repository-url>
cd budget-api
docker compose up --build
```
When you see a log line like `Started BudgetApiApplication`, the API is ready at:

```
http://localhost:8080
```

### Stopping and resetting

```bash
# Stop (Ctrl+C if running in the foreground), then remove the containers:
docker compose down

# Run in the background instead:
docker compose up --build -d

# Follow the application logs:
docker compose logs -f app
```

> **Data is not persisted between `docker compose down` and the next `up`** — this is intentional, so every run starts from a clean, reproducible state
---

## Running the tests

The test suite (unit + integration) runs against an **in-memory H2 database**

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
---

## Configuration

| Environment variable           | Default (local)                                  | Value in Docker Compose                       |
| ------------------------------ | ------------------------------------------------ | --------------------------------------------- |
| `SPRING_DATASOURCE_URL`        | `jdbc:postgresql://localhost:5433/budget_db`     | `jdbc:postgresql://db:5432/budget_db`         |
| `SPRING_DATASOURCE_USERNAME`   | `postgres`                                       | `budget`                                      |
| `SPRING_DATASOURCE_PASSWORD`   | `admin123`                                       | `budget`                                      |
| `SPRING_JPA_HIBERNATE_DDL_AUTO`| `update`                                         | `update`                                      |


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

