CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE IF NOT EXISTS derived_metrics (
    timestamp TIMESTAMPTZ NOT NULL,
    region VARCHAR(50) NOT NULL,
    metric VARCHAR(50) NOT NULL,
    value DOUBLE PRECISION,
    PRIMARY KEY (timestamp, region, metric)
);

SELECT create_hypertable(
   'derived_metrics',
   'timestamp',
    if_not_exists => TRUE
);

CREATE TABLE IF NOT EXISTS smoothed_metrics (
    timestamp TIMESTAMPTZ NOT NULL,
    region VARCHAR(50) NOT NULL,
    metric VARCHAR(50) NOT NULL,
    source VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    value DOUBLE PRECISION,
    PRIMARY KEY (timestamp, region, metric, source, category)
);

SELECT create_hypertable(
   'smoothed_metrics',
   'timestamp',
   if_not_exists => TRUE
);

CREATE INDEX IF NOT EXISTS idx_derived_lookup ON derived_metrics (region, metric, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_smoothed_lookup ON smoothed_metrics (region, source, timestamp DESC);

