# Vehicle Health Analysis Platform

Collects vehicle telemetry, analyzes vehicle condition, computes a health score and raises alerts.

Work in progress — full documentation will be added as the project evolves.

## Health score rules

The score starts at **100**. Each rule subtracts a penalty; the final score is bounded to **0**.
The statement gives no figures, so the thresholds below are documented business assumptions.

| Rule | Condition | Penalty | Alert |
|---|---|---|---|
| Mileage | > 100,000 km | -5 | none |
| | > 150,000 km | -10 | WARNING |
| | > 200,000 km | -15 | WARNING |
| Tire pressure (per tire) | outside [1.9, 3.0] bar | -5 | WARNING |
| | outside [1.5, 3.5] bar | -10 | CRITICAL |
| Battery | < 12.2 V | -10 | WARNING |
| | < 11.8 V | -20 | CRITICAL |
| Engine temperature | > 105 °C | -10 | WARNING |
| | > 115 °C | -25 | CRITICAL |
| Fuel | < 10 % | -5 | WARNING |
| Error codes | -10 per distinct code | capped at -30 | WARNING per code |
| Recall campaigns | -15 per open campaign | capped at -30 | CRITICAL per campaign |

Health status:

| Score | Status |
|---|---|
| >= 80 | `GOOD` |
| >= 50 | `ATTENTION_REQUIRED` |
| < 50 | `CRITICAL` |

A vehicle with at least one CRITICAL alert can never be `GOOD`: its status is at least `ATTENTION_REQUIRED`.

Each rule is an implementation of `HealthRule` (strategy pattern): adding a rule means adding a class,
without modifying the calculator.
