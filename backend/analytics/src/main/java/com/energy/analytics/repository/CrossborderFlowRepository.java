package com.energy.analytics.repository;

import com.energy.analytics.model.entity.FlowPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.ZoneOffset;
import java.util.List;

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
}
