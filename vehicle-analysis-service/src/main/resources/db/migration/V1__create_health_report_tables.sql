CREATE TABLE health_report (
    id                     UUID PRIMARY KEY,
    vin                    VARCHAR(17)              NOT NULL,
    telemetry_id           UUID                     NOT NULL,
    telemetry_recorded_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    analyzed_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    score                  INTEGER                  NOT NULL,
    status                 VARCHAR(30)              NOT NULL,
    CONSTRAINT uk_health_report_telemetry_id UNIQUE (telemetry_id)
);

CREATE INDEX idx_health_report_vin_recorded_at ON health_report (vin, telemetry_recorded_at DESC);

CREATE TABLE health_alert (
    report_id    UUID         NOT NULL REFERENCES health_report (id) ON DELETE CASCADE,
    alert_index  INTEGER      NOT NULL,
    type         VARCHAR(30)  NOT NULL,
    severity     VARCHAR(10)  NOT NULL,
    message      VARCHAR(255) NOT NULL,
    PRIMARY KEY (report_id, alert_index)
);
