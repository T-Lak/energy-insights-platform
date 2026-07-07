package com.energy.analytics.repository;

import com.energy.analytics.model.entity.FlowPoint;
import com.energy.analytics.model.entity.Metric;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GridAnalyticsRepository {

   private final JdbcTemplate jdbcTemplate;

   public List<Metric> getGridSnapshot(Instant ts, String region) {
      String sql = """
        WITH latest_snapshot AS (
            SELECT MAX(timestamp) as max_ts
            FROM energy_metrics
            WHERE region = ? AND timestamp <= ?
        ),
        smoothed_metrics AS (
            SELECT timestamp, metric, source, category,
               AVG(value) OVER (
                   PARTITION BY region, metric, source, category
                   ORDER BY timestamp
                   RANGE BETWEEN INTERVAL '2 hours' PRECEDING AND CURRENT ROW
               ) AS smoothed_value
            FROM energy_metrics
            CROSS JOIN latest_snapshot
            WHERE region = ?
              AND timestamp BETWEEN (latest_snapshot.max_ts - INTERVAL '2 hours') AND latest_snapshot.max_ts
        )
        SELECT sm.* FROM smoothed_metrics sm
        CROSS JOIN latest_snapshot ls
        WHERE sm.timestamp = ls.max_ts;
      """;

      return jdbcTemplate.query(
              sql,
              (rs, rowNum) -> new Metric(
                      rs.getTimestamp("timestamp").toInstant(),
                      region,
                      rs.getString("metric"),
                      rs.getString("source"),
                      rs.getString("category"),
                      rs.getDouble("smoothed_value")
              ),
              region,
              ts.atOffset(ZoneOffset.UTC),
              region
      );
   }

   public List<FlowPoint> getFlowPoints(Instant ts, String region) {
      String sql = """
         SELECT timestamp, from_region, to_region, export_mw, import_mw
         FROM crossborder_flows
         WHERE timestamp = ? AND from_region = ?
      """;

      return jdbcTemplate.query(
              sql,
              (rs, rowNum) -> new FlowPoint(
                      rs.getTimestamp("timestamp").toInstant(),
                      rs.getString("from_region"),
                      rs.getString("to_region"),
                      rs.getFloat("export_mw"),
                      rs.getFloat("import_mw")
              ),
              ts.atOffset(ZoneOffset.UTC),
              region
      );
   }

}
