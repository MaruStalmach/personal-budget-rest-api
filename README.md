# SoftNet-recruitment-task

Simple Spring Boot budget API used for the SoftNet recruitment task.

## Prerequisites

- Java 17+ or 21 (runtime for local build is provided by the Maven wrapper)
- Docker & docker-compose (optional, for containerized run)
- Git (recommended)

## Build (local)

Use the included Maven wrapper to build the project:

```
./mvnw clean package -DskipTests
```

This produces a runnable jar in `target/`.

## Run (local)

Run the packaged jar:

```
java -jar target/*.jar
```

Alternatively, run the app directly from Maven (dev mode):

```
./mvnw spring-boot:run
```

The application listens on port 8080 by default. Configuration is in `src/main/resources/application.properties`.

## Tests

Run unit & integration tests with:

```
./mvnw test
```

Test properties are located at `src/test/resources/application-test.properties`.

## Docker

Build the image with the provided `Dockerfile`:

```
docker build -t budget-api:local .
```

Run with Docker:

```
docker run --rm -p 8080:8080 \
	-e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/budget_db \
	-e SPRING_DATASOURCE_USERNAME=admin \
	-e SPRING_DATASOURCE_PASSWORD=admin123 \
	budget-api:local
```

## Docker Compose (Postgres + API)

There is a `docker-compose.yml` that brings up a Postgres database and the API:

```
docker-compose up --build
```

It creates a Postgres service on port `5432` and the API on `8080`. Environment variables are defined in the compose file.

## Environment variables

- `SPRING_DATASOURCE_URL` — JDBC URL for Postgres
- `SPRING_DATASOURCE_USERNAME` — DB username
- `SPRING_DATASOURCE_PASSWORD` — DB password
- `SPRING_JPA_HIBERNATE_DDL_AUTO` — Hibernate DDL mode (e.g., `update`, `validate`)

## Troubleshooting

- If the app cannot connect to Postgres when using Docker Compose, ensure the `db` service is healthy and waiting for it (compose uses healthcheck).
- For build issues, run `./mvnw -e -X <goal>` to see a full stacktrace.

## Contributing

Feel free to open issues or submit pull requests. Keep changes small and focused; include tests for behaviour changes.

---

Updated README with build, run, test and Docker instructions.
