package com.energy.analytics.repository;

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
            SUM(export_mw) AS total_export,
            SUM(import_mw) AS total_import
        FROM crossborder_flows
        WHERE timestamp BETWEEN ? AND ?
          AND from_region = ?
        GROUP BY timestamp
        ORDER BY timestamp
        """;

      return jdbcTemplate.query(
              sql,
              (rs, rowNum) -> {
                 float totalExport = rs.getFloat("total_export");
                 float totalImport = rs.getFloat("total_import");

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

}
