package com.energy.analytics.repository;

import com.energy.analytics.model.entity.DerivedMetric;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.ZoneOffset;
import java.util.List;

@Repository
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
}
