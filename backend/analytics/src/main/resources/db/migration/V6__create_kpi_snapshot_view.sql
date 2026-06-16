CREATE MATERIALIZED VIEW view_latest_kpi_snapshot
WITH (
   timescaledb.continuous,
   timescaledb.materialized_only = false
) AS
SELECT
    time_bucket('15 minute', timestamp) AS bucket,
    region,
    MAX(CASE WHEN metric = 'renewable_share' THEN value END) as renewable_share,
    MAX(CASE WHEN metric = 'carbon_intensity' THEN value END) as carbon_intensity,
    MAX(CASE WHEN metric = 'total_load' THEN value END) as total_load,
    MAX(CASE WHEN metric = 'net_balance' THEN value END) as net_balance
FROM derived_metrics
GROUP BY bucket, region
WITH NO DATA;

SELECT add_continuous_aggregate_policy('view_latest_kpi_snapshot',
                                       start_offset => INTERVAL '1 hour',
                                       end_offset => INTERVAL '0 minutes',
                                       schedule_interval => INTERVAL '15 minute');