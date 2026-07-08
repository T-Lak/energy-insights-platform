package com.energy.analytics.unit.repository;

import com.energy.analytics.BaseContainerTest;
import com.energy.analytics.crossborder.model.FlowPoint;
import com.energy.analytics.ingestion.model.Metric;
import com.energy.analytics.dashboard.repository.GridAnalyticsRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class GridAnalyticsRepositoryTest extends BaseContainerTest {

   @Autowired
   private GridAnalyticsRepository repository;

   @Autowired
   private JdbcTemplate jdbcTemplate;

   private final Instant targetTime = Instant.parse("2026-07-08T12:00:00Z");
   private final Instant oneHourAgo = Instant.parse("2026-07-08T11:00:00Z");
   private final Instant twoHoursAgo = Instant.parse("2026-07-08T10:00:00Z");
   private final Instant threeHoursAgo = Instant.parse("2026-07-08T09:00:00Z");

   @BeforeEach
   void setUp() {
      jdbcTemplate.execute("DELETE FROM energy_metrics");
      jdbcTemplate.execute("DELETE FROM crossborder_flows");
   }

   @AfterEach
   void cleanUp() {
      jdbcTemplate.execute("DELETE FROM energy_metrics");
      jdbcTemplate.execute("DELETE FROM crossborder_flows");
   }

   @Test
   @DisplayName("Should fetch grid snapshot using window moving average calculations over a 2-hour interval")
   void getGridSnapshot_ValidatesTimeRangeMovingAverage() {
      String region = "DE";

      jdbcTemplate.execute(String.format("""
         INSERT INTO energy_metrics (timestamp, region, metric, source, category, value) VALUES 
         ('%s', 'DE', 'generation', 'solar', 'actual', 100.0), -- 12:00 (target/latest)
         ('%s', 'DE', 'generation', 'solar', 'actual', 200.0), -- 11:00 (1 hour ago, included in moving average)
         ('%s', 'DE', 'generation', 'solar', 'actual', 300.0), -- 10:00 (2 hours ago, boundary included)
         ('%s', 'DE', 'generation', 'solar', 'actual', 400.0), -- 09:00 (3 hours ago, should be ignored by interval)
         ('%s', 'FR', 'generation', 'solar', 'actual', 500.0); -- Different region, should be filtered out entirely
      """, targetTime, oneHourAgo, twoHoursAgo, threeHoursAgo, targetTime));

      List<Metric> results = repository.getGridSnapshot(targetTime, region);

      assertThat(results).hasSize(1);
      Metric solarMetric = results.get(0);

      assertThat(solarMetric.getTimestamp()).isEqualTo(targetTime);
      assertThat(solarMetric.getRegion()).isEqualTo("DE");
      assertThat(solarMetric.getSource()).isEqualTo("solar");

      assertThat(solarMetric.getValue()).isEqualTo(200.0);
   }

   @Test
   @DisplayName("Should extract precise flow points filtering exactly by timestamp and source region")
   void getFlowPoints_FiltersAccuratelyByTimeAndRegion() {
      String region = "DE";

      jdbcTemplate.execute(String.format("""
         INSERT INTO crossborder_flows (timestamp, from_region, to_region, export_mw, import_mw) VALUES 
         ('%s', 'DE', 'FR', 600.0, 150.0), -- Exact match
         ('%s', 'DE', 'NL', 400.0, 50.0),  -- Exact match
         ('%s', 'FR', 'DE', 200.0, 100.0), -- Wrong from_region
         ('%s', 'DE', 'FR', 900.0, 300.0); -- Wrong timestamp
      """, targetTime, targetTime, targetTime, oneHourAgo));

      List<FlowPoint> results = repository.getFlowPoints(targetTime, region);

      assertThat(results).hasSize(2);

      FlowPoint frFlow = results.stream().filter(f -> f.getToRegion().equals("FR")).findFirst().orElseThrow();
      assertThat(frFlow.getTimestamp()).isEqualTo(targetTime);
      assertThat(frFlow.getFromRegion()).isEqualTo("DE");
      assertThat(frFlow.getExportMW()).isEqualTo(600.0f);
      assertThat(frFlow.getImportMW()).isEqualTo(150.0f);

      FlowPoint nlFlow = results.stream().filter(f -> f.getToRegion().equals("NL")).findFirst().orElseThrow();
      assertThat(nlFlow.getExportMW()).isEqualTo(400.0f);
   }
}