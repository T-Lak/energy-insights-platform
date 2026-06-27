CREATE MATERIALIZED VIEW view_crossborder_flows_hourly
WITH (
   timescaledb.continuous,
   timescaledb.materialized_only = false
) AS
SELECT
    time_bucket('1 hour', timestamp) AS bucket,
    from_region,
    to_region,
    AVG(export_mw) AS avg_export_mw,
    AVG(import_mw) AS avg_import_mw
FROM crossborder_flows
GROUP BY bucket, from_region, to_region
WITH NO DATA;

SELECT add_continuous_aggregate_policy('view_crossborder_flows_hourly',
                                       start_offset => INTERVAL '3 hours',
                                       end_offset => INTERVAL '1 hour',
                                       schedule_interval => INTERVAL '1 hour');

CREATE MATERIALIZED VIEW view_crossborder_flows_daily
WITH (
   timescaledb.continuous,
   timescaledb.materialized_only = false
) AS
SELECT
    time_bucket('1 day', timestamp) AS bucket,
    from_region,
    to_region,
    AVG(export_mw) AS avg_export_mw,
    AVG(import_mw) AS avg_import_mw
FROM crossborder_flows
GROUP BY bucket, from_region, to_region
WITH NO DATA;

SELECT add_continuous_aggregate_policy('view_crossborder_flows_daily',
                                       start_offset => INTERVAL '3 days',
                                       end_offset => INTERVAL '1 day',
                                       schedule_interval => INTERVAL '1 day');