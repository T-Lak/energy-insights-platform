SELECT create_hypertable(
   'energy_metrics',
   'timestamp',
   if_not_exists => TRUE
);