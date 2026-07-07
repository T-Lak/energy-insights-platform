    CREATE MATERIALIZED VIEW view_energy_metrics_daily_raw
WITH (
    timescaledb.continuous = true,
    timescaledb.materialized_only = false
) AS
SELECT
    time_bucket('1 hour', timestamp) AS bucket_hour,
    region,
    source,
    SUM(value) AS total_mwh
FROM energy_metrics
WHERE metric = 'generation' AND category = 'actual aggregated'
GROUP BY bucket_hour, region, source
WITH NO DATA;

SELECT add_continuous_aggregate_policy('view_energy_metrics_daily_raw',
                                       start_offset      => INTERVAL '7 days',
                                       end_offset        => INTERVAL '1 hour',
                                       schedule_interval => INTERVAL '1 hour');

CREATE OR REPLACE VIEW view_energy_metrics_daily_summary AS
SELECT
    time_bucket('1 day', bucket_hour) AS production_day,
    region,
    CASE
        WHEN EXTRACT(HOUR FROM bucket_hour) BETWEEN 6 AND 17 THEN 'Day'
        ELSE 'Night'
        END AS time_period,
    CASE
        WHEN source IN (
            'solar', 'wind onshore', 'wind offshore', 'biomass', 'geothermal',
            'hydro run-of-river and poundage', 'hydro water reservoir', 'other renewable'
        ) THEN 'Renewables'
        ELSE 'Fossils'
        END AS energy_category,
    source AS precise_source,
    SUM(total_mwh) AS total_mwh
FROM view_energy_metrics_daily_raw
GROUP BY production_day, region, time_period, energy_category, precise_source;