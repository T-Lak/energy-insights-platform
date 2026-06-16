package com.energy.analytics.repository;

import com.energy.analytics.dto.websocket.model.FlowTotalsDTO;
import com.energy.analytics.model.entity.FlowPoint;
import com.energy.analytics.model.entity.Metric;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GridAnalyticsRepository {

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

   public Optional<FlowTotalsDTO> getFlowTotals(Instant ts, String region) {
      String sql = """
         SELECT
            SUM(export_mw) as total_export,
            SUM(import_mw) as total_import
          FROM crossborder_flows
          WHERE timestamp = ? AND from_region = ?
      """;

      return jdbcTemplate.query(
              sql,
              rs -> {
                 if (rs.next()) {
                    float totalExport = rs.getFloat("total_export");
                    float totalImport = rs.getFloat("total_import");

                    return Optional.of(new FlowTotalsDTO(
                            ts,
                            region,
                            totalExport,
                            totalImport,
                            totalExport - totalImport
                    ));
                 }
                 return Optional.empty();
              },
              ts.atOffset(ZoneOffset.UTC),
              region
      );
   }

}
