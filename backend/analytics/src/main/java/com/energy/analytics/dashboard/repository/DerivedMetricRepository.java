package com.energy.analytics.dashboard.repository;

import com.energy.analytics.ingestion.repository.BatchRepository;
import com.energy.analytics.dashboard.model.DerivedMetric;
import com.energy.analytics.dashboard.model.KpiSnapshotView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

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

   public Optional<KpiSnapshotView> findLatestSnapshotByRegion(String region) {
      String sql = """
         SELECT bucket, region, renewable_share, carbon_intensity, total_load, net_balance
         FROM view_latest_kpi_snapshot
         WHERE region = ?
         ORDER BY bucket DESC
         LIMIT 1
      """;

      try {
         KpiSnapshotView snapshot = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            KpiSnapshotView view = new KpiSnapshotView();

            if (rs.getTimestamp("bucket") != null) {
               view.setBucket(rs.getTimestamp("bucket").toInstant());
            }

            view.setRegion(rs.getString("region"));
            view.setRenewableShare(rs.getDouble("renewable_share"));
            view.setCarbonIntensity(rs.getDouble("carbon_intensity"));
            view.setTotalLoad(rs.getDouble("total_load"));
            view.setNetBalance(rs.getDouble("net_balance"));

            return view;
         }, region);

         return Optional.ofNullable(snapshot);
      } catch (EmptyResultDataAccessException e) {
         log.info("No data for region {} found", region);
         return Optional.empty();
      }
   }
}
