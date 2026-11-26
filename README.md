# Vehicle Health Analysis Platform

Collects telemetry from connected vehicles, analyzes their condition, computes a health score
and raises alerts. Two Spring Boot microservices communicate asynchronously through Kafka.

## Architecture
cd "$(git rev-parse --show-toplevel)"

cat > README.md <<'EOF'
# Vehicle Health Analysis Platform

Two Spring Boot microservices that collect vehicle telemetry, compute a health score and raise alerts.

```
POST /telemetry ──▶ telemetry-service (8081) ──Kafka──▶ vehicle-analysis-service (8082) ──▶ GET health-report / alerts
                          │                                     │
                     PostgreSQL                     PostgreSQL + recall API (WireMock)
```

## Stack

Java 21 · Spring Boot 4.2.0 · Maven 3.10.0 (wrapper) · PostgreSQL 17 · Kafka 4.1.0 · WireMock 3.13.1 · Docker

## Run

```bash
docker compose --profile app up -d --build
```

Swagger UI: http://localhost:8081/swagger-ui.html and http://localhost:8082/swagger-ui.html
Ready-to-run requests: `http/*.http`

## API

| Method | Path | Response |
|---|---|---|
| POST | `/api/v1/telemetry` | `202` accepted, `400` invalid payload |
| GET | `/api/v1/vehicles/{vin}/health-report` | `200`, `404` no report |
| GET | `/api/v1/vehicles/{vin}/alerts?severity=` | `200`, `404` no report |

## Tests

```bash
cd telemetry-service && ./mvnw verify
cd vehicle-analysis-service && ./mvnw verify
```

Unit tests (domain), MockMvc tests (controllers), integration tests (Testcontainers + WireMock).
The 4 acceptance scenarios are covered by `TelemetryIngestionIntegrationTest` and `VehicleHealthAcceptanceTest`.

## Health score

Starts at 100, each rule subtracts a penalty (assumed thresholds, the statement gives none):
mileage (-5 to -15), tire pressure (-5/-10 per tire), battery (-10/-20), engine temperature (-10/-25),
fuel (-5), error codes (-10 each, max -30), open recall campaigns (-15 each, max -30).
Status: `GOOD` ≥ 80, `ATTENTION_REQUIRED` ≥ 50, `CRITICAL` < 50.

## Choices

- Hexagonal architecture: domain without Spring, one use case per action.
- Asynchronous analysis via Kafka (key = VIN), idempotent consumer, retries then dead letter topic.
- One database per service, schema managed by Flyway.

