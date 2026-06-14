CREATE TABLE crossborder_flows (
    timestamp TIMESTAMPTZ NOT NULL,
    from_region VARCHAR(50) NOT NULL,
    to_region VARCHAR(50) NOT NULL,
    export_mw REAL NOT NULL DEFAULT 0.0,
    import_mw REAL NOT NULL DEFAULT 0.0,

    CONSTRAINT pk_crossborder_flows PRIMARY KEY (timestamp, from_region, to_region)
);

SELECT create_hypertable('crossborder_flows', 'timestamp', chunk_time_interval => INTERVAL '7 days');

CREATE INDEX idx_flows_region_time ON crossborder_flows (from_region, timestamp DESC);