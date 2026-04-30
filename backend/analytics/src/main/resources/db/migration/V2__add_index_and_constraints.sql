CREATE INDEX idx_metrics_time_metric
    ON energy_metrics (timestamp DESC, metric);