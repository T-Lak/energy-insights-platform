package com.energy.analytics.unit.repository;

import com.energy.analytics.BaseContainerTest;
import com.energy.analytics.dashboard.model.DerivedMetric;
import com.energy.analytics.dashboard.model.KpiSnapshotView;
import com.energy.analytics.dashboard.repository.DerivedMetricRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DerivedMetricsRepositoryTest extends BaseContainerTest {

   @Autowired
   private DerivedMetricRepository repository;

   @Autowired
   private JdbcTemplate jdbcTemplate;

   private final Instant now = Instant.parse("2026-07-08T12:00:00Z");
   private final Instant anHourAgo = now.minusSeconds(3600);

   @AfterEach
   void cleanUp() {
      jdbcTemplate.execute("DELETE FROM derived_metrics");
   }

   @Test
   @DisplayName("Should insert derived metrics in batch")
   void upsertBatch_InsertsMetrics() {
      List<DerivedMetric> metrics = List.of(
              new DerivedMetric(now, "DE", "renewable_share", 75.0),
              new DerivedMetric(now, "DE", "carbon_intensity", 230.5)
      );

      repository.upsertBatch(metrics);

      Integer count = jdbcTemplate.queryForObject(
              """
              SELECT COUNT(*)
              FROM derived_metrics
              WHERE region = 'DE'
              """,
              Integer.class
      );

      assertThat(count).isEqualTo(2);
   }


   @Test
   @DisplayName("Should update existing metric on conflict")
   void upsertBatch_UpdatesExistingMetric() {

      repository.upsertBatch(List.of(
              new DerivedMetric(
                      now,
                      "DE",
                      "renewable_share",
                      60.0
              )
      ));

      repository.upsertBatch(List.of(
              new DerivedMetric(
                      now,
                      "DE",
                      "renewable_share",
                      80.0
              )
      ));

      Double value = jdbcTemplate.queryForObject(
              """
              SELECT value
              FROM derived_metrics
              WHERE timestamp = ?
                AND region = ?
                AND metric = ?
              """,
              Double.class,
              java.sql.Timestamp.from(now),
              "DE",
              "renewable_share"
      );

      assertThat(value).isEqualTo(80.0);
   }

   @Disabled
   @Test
   @DisplayName("Should return latest KPI snapshot for region")
   void findLatestSnapshotByRegion_ReturnsLatestSnapshot() {
      repository.upsertBatch(List.of(
              new DerivedMetric(anHourAgo, "DE", "renewable_share", 60.0),
              new DerivedMetric(anHourAgo, "DE", "carbon_intensity", 250.0),
              new DerivedMetric(anHourAgo, "DE", "total_load", 50000.0),
              new DerivedMetric(anHourAgo, "DE", "net_balance", -1000.0),

              new DerivedMetric(now, "DE", "renewable_share", 75.0),
              new DerivedMetric(now, "DE", "carbon_intensity", 200.0),
              new DerivedMetric(now, "DE", "total_load", 60000.0),
              new DerivedMetric(now, "DE", "net_balance", 500.0)
      ));

      Optional<KpiSnapshotView> result =
              repository.findLatestSnapshotByRegion("DE");

      assertThat(result).isPresent();

      KpiSnapshotView snapshot = result.get();
      assertThat(snapshot.getRegion()).isEqualTo("DE");

      assertThat(snapshot.getBucket()).isNotNull();
      assertThat(snapshot.getRenewableShare()).isEqualTo(75.0);
      assertThat(snapshot.getCarbonIntensity()).isEqualTo(200.0);
      assertThat(snapshot.getTotalLoad()).isEqualTo(60000.0);
      assertThat(snapshot.getNetBalance()).isEqualTo(500.0);
   }


   @Test
   @DisplayName("Should return empty when no KPI snapshot exists")
   void findLatestSnapshotByRegion_NoData_ReturnsEmpty() {
      Optional<KpiSnapshotView> result =
              repository.findLatestSnapshotByRegion("FR");

      assertThat(result).isEmpty();
   }
}