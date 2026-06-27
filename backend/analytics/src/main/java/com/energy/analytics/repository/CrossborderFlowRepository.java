package com.energy.analytics.repository;

import com.energy.analytics.dto.websocket.model.FlowPointDTO;
import com.energy.analytics.dto.websocket.model.FlowTotalsDTO;
import com.energy.analytics.model.entity.FlowPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CrossborderFlowRepository implements BatchRepository<FlowPoint> {

   private final JdbcTemplate jdbcTemplate;

   @Override
   public void upsertBatch(List<FlowPoint> flows) {
      String sql = """
           INSERT INTO crossborder_flows (
               timestamp, from_region, to_region, export_mw, import_mw
           ) VALUES (?, ?, ?, ?, ?)
           ON CONFLICT (timestamp, from_region, to_region)
           DO UPDATE SET
               export_mw = EXCLUDED.export_mw,
               import_mw = EXCLUDED.import_mw
      """;

      jdbcTemplate.batchUpdate(
              sql,
              flows,
              50,
              (ps, f) -> {
                 ps.setObject(1, f.getTimestamp().atOffset(ZoneOffset.UTC));
                 ps.setObject(2, f.getFromRegion());
                 ps.setObject(3, f.getToRegion());
                 ps.setObject(4, f.getExportMW());
                 ps.setObject(5, f.getImportMW());
              }
      );
   }

   public List<FlowTotalsDTO> getFlowTotals(
           Instant start,
           Instant end,
           String region
   ) {
      String sql = """
        SELECT
            timestamp,
            SUM(export_mw) AS total_export_mw,
            SUM(import_mw) AS total_import_mw
        FROM crossborder_flows
        WHERE timestamp BETWEEN ? AND ?
          AND from_region = ?
        GROUP BY timestamp
        ORDER BY timestamp
        """;

      return jdbcTemplate.query(
              sql,
              (rs, rowNum) -> {
                 float totalExport = rs.getFloat("total_export_mw");
                 float totalImport = rs.getFloat("total_import_mw");

                 return new FlowTotalsDTO(
                         rs.getObject("timestamp", OffsetDateTime.class).toInstant(),
                         region,
                         totalExport,
                         totalImport,
                         totalExport - totalImport
                 );
              },
              start.atOffset(ZoneOffset.UTC),
              end.atOffset(ZoneOffset.UTC),
              region
      );
   }

   public Optional<FlowTotalsDTO> getFlowTotals(
           Instant ts,
           String region
   ) {
      return getFlowTotals(ts, ts, region)
              .stream()
              .findFirst();
   }

   public List<FlowPointDTO> getLatestFlowPoints(String region) {
      String sql = """
         SELECT
            MAX(timestamp) AS timestamp,
            from_region,
            split_part(to_region, '_', 1) AS to_region,
            SUM(export_mw) AS total_export_mw,
            SUM(import_mw) AS total_import_mw
         FROM crossborder_flows
         WHERE timestamp = (SELECT MAX(timestamp) FROM crossborder_flows WHERE from_region = ?)
            AND from_region = ?
         GROUP BY from_region, split_part(to_region, '_', 1);
      """;

      return jdbcTemplate.query(
              sql,
              (rs, rowNum) -> {
                 float totalExport = rs.getFloat("total_export_mw");
                 float totalImport = rs.getFloat("total_import_mw");

                 return new FlowPointDTO(
                      rs.getObject("timestamp", OffsetDateTime.class).toInstant(),
                      rs.getString("from_region"),
                      rs.getString("to_region"),
                      totalExport,
                      totalImport
                 );
              },
              region,
              region
      );
   }

   public List<FlowPointDTO> getWeeklyFlows(Instant start, Instant end, String region) {
      return getFlowsFromView("view_crossborder_flows_hourly", start, end, region);
   }

   public List<FlowPointDTO> getMonthlyFlows(Instant start, Instant end, String region) {
      return getFlowsFromView("view_crossborder_flows_daily", start, end, region);
   }

   public List<FlowPointDTO> getFlowsFromView(String viewName, Instant start, Instant end, String region) {
      String sql = String.format("""
       SELECT
         bucket,
         from_region,
         to_region,
         avg_export_mw,
         avg_import_mw
      FROM %s
      WHERE bucket BETWEEN ? AND ?
      AND from_region = ?
      ORDER BY bucket, to_region
   """, viewName);

      return jdbcTemplate.query(
           sql,
           (rs, rowNum) -> new FlowPointDTO(
                rs.getObject("bucket", OffsetDateTime.class).toInstant(),
                rs.getString("from_region"),
                rs.getString("to_region"),
                rs.getFloat("avg_export_mw"),
                rs.getFloat("avg_import_mw")
           ),
           start.atOffset(ZoneOffset.UTC),
           end.atOffset(ZoneOffset.UTC),
           region
      );
   }

}
