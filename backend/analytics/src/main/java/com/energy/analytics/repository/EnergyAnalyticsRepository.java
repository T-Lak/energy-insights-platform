package com.energy.analytics.repository;

import com.energy.analytics.model.entity.Metric;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnergyAnalyticsRepository {

   private final JdbcTemplate jdbcTemplate;

   public List<Metric> getGridSnapshot(Instant ts, String region) {

      String sql = """
           SELECT timestamp, metric, source, category, smoothed_value
           FROM (
               SELECT
                   timestamp,
                   metric,
                   source,
                   category,
                   AVG(value) OVER (
                       PARTITION BY region, metric, source, category
                       ORDER BY timestamp
                       RANGE BETWEEN INTERVAL '2 hours' PRECEDING AND CURRENT ROW
                   ) AS smoothed_value
                FROM energy_metrics
                WHERE region = ?
           ) t
           WHERE timestamp = ?
           ORDER BY timestamp;
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
              ts.atOffset(ZoneOffset.UTC)
      );
   }

}
