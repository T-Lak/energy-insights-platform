CREATE MATERIALIZED VIEW view_daily_renewable_summary
WITH (
    timescaledb.continuous,
    timescaledb.materialized_only = false
) AS
SELECT
    time_bucket('1 hour', timestamp) AS bucket,
    source,
    region,
    AVG(value) AS avg_generation_mw
FROM energy_metrics
WHERE metric = 'generation'
AND category LIKE '%aggregated%'
AND source IN (
   'solar', 'wind onshore', 'wind offshore', 'biomass', 'geothermal',
   'hydro run-of-river and poundage', 'hydro water reservoir', 'other renewable'
)
GROUP BY bucket, source, region
WITH NO DATA;

SELECT add_continuous_aggregate_policy('view_daily_renewable_summary',
                                       start_offset => INTERVAL '365 days',
                                       end_offset => INTERVAL '15 minutes',
                                       schedule_interval => INTERVAL '15 minutes');
