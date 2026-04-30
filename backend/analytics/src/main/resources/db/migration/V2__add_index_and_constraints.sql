CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE energy_metrics (
    timestamp TIMESTAMPTZ NOT NULL,
    region VARCHAR(50) NOT NULL,
    metric VARCHAR(50) NOT NULL,
    source VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    value DOUBLE PRECISION,
    PRIMARY KEY (timestamp, region, metric, source, category)
);

SELECT create_hypertable('energy_metrics', 'timestamp');

CREATE INDEX idx_metrics_region_source
    ON energy_metrics (region, source, timestamp DESC);
CREATE INDEX idx_metrics_time_metric
    ON energy_metrics (timestamp DESC, metric);