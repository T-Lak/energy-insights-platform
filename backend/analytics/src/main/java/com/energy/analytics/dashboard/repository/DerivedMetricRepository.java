package com.energy.analytics.dashboard.repository;

import com.energy.analytics.ingestion.repository.BatchRepository;
import com.energy.analytics.dashboard.model.DerivedMetric;
import com.energy.analytics.dashboard.model.KpiSnapshotView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.ZoneOffset;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DerivedMetricRepository implements BatchRepository<DerivedMetric> {

   private final JdbcTemplate jdbcTemplate;

   @Override
   public void upsertBatch(List<DerivedMetric> metrics) {
      String sql = """
         INSERT INTO derived_metrics (
            timestamp, region, metric, value
         ) VALUES (?, ?, ?, ?)
         ON CONFLICT (timestamp, region, metric)
         DO UPDATE SET value = EXCLUDED.value
      """;

      jdbcTemplate.batchUpdate(
           sql,
           metrics,
           50,
           (ps, m) -> {
              ps.setObject(1, m.getTimestamp().atOffset(ZoneOffset.UTC));
              ps.setObject(2, m.getRegion());
              ps.setObject(3, m.getMetric());
              ps.setObject(4, m.getValue());
           }
      );
   }

   public List<KpiSnapshotView> findLatestSnapshotsByRegion(String region) {
      String sql = """
         SELECT bucket, region, renewable_share, carbon_intensity, total_load, net_balance
         FROM view_latest_kpi_snapshot
         WHERE region = ?
         ORDER BY bucket DESC
         LIMIT 2
      """;

      return jdbcTemplate.query(
         sql,
         (rs, rowNum) -> new KpiSnapshotView(
                rs.getTimestamp("bucket").toInstant(),
                rs.getString("region"),
                rs.getDouble("renewable_share"),
                rs.getDouble("carbon_intensity"),
                rs.getDouble("total_load"),
                rs.getDouble("net_balance")
         ),
         region
      );
   }
}
