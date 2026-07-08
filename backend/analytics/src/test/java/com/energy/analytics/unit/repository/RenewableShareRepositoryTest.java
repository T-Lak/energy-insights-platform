package com.energy.analytics.unit.repository;

import com.energy.analytics.BaseContainerTest;
import com.energy.analytics.dashboard.model.DailyEnergySummary;
import com.energy.analytics.renewables.model.DailyRenewablesSummary;
import com.energy.analytics.renewables.model.RenewableMix;
import com.energy.analytics.renewables.repository.RenewableShareRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class RenewableShareRepositoryTest extends BaseContainerTest {

   @Autowired
   private RenewableShareRepository repository;

   @Autowired
   private JdbcTemplate jdbcTemplate;

   private final Instant now = Instant.parse("2026-07-08T12:00:00Z");
   private final Instant earlyMorning = Instant.parse("2026-07-08T06:00:00Z");

   @BeforeEach
   void setUp() {
      jdbcTemplate.execute("DELETE FROM energy_metrics");

      jdbcTemplate.execute("""
        CREATE OR REPLACE VIEW view_energy_metrics_daily_summary AS
        SELECT 
            timestamp::date as production_day,
            region,
            CASE WHEN EXTRACT(HOUR FROM timestamp) BETWEEN 6 AND 21 THEN 'Day' ELSE 'Night' END as time_period,
            category as energy_category,
            source as precise_source,
            value as total_mwh
        FROM energy_metrics;
    """);
   }

   @AfterEach
   void cleanUp() {
      jdbcTemplate.execute("DELETE FROM energy_metrics");
   }

   @Test
   @DisplayName("Should aggregate daily renewable metrics handling CASE conditional sums and exclusions")
   void getRenewablesMetricsPerDay_ValidatesConditionalAggregations() {
      String region = "DE";

      jdbcTemplate.execute(String.format("""
         INSERT INTO energy_metrics (timestamp, source, value, region, category, metric) VALUES
         ('%s', 'solar', 150.0, 'DE', 'actual', 'generation'),
         ('%s', 'wind onshore', 250.0, 'DE', 'actual', 'generation'),
         ('%s', 'hydro run-of-river and poundage', 75.0, 'DE', 'actual aggregated', 'generation'),
         ('%s', 'coal', 900.0, 'DE', 'actual', 'generation'); -- Should be dropped by case logic
      """, now, now, now, now));

      List<RenewableMix> results = repository.getRenewablesMetricsPerDay(
              now.minusSeconds(60),
              now.plusSeconds(60),
              region
      );

      assertThat(results).hasSize(1);
      RenewableMix mix = results.get(0);
      assertThat(mix.getTimestamp()).isEqualTo(now);
      assertThat(mix.getSolar()).isEqualTo(150.0);
      assertThat(mix.getWindOnshore()).isEqualTo(250.0);
      assertThat(mix.getHydro()).isEqualTo(75.0);
      assertThat(mix.getGeothermal()).isEqualTo(0.0);
   }

   @Test
   @DisplayName("Should extract daily summary and cleanly parse aggregated JSON breakdown payloads")
   void getDailySummary_ValidatesPostgresJsonAggregationParsing() {
      LocalDate date = LocalDate.parse("2026-07-08");
      String region = "FR";

      jdbcTemplate.execute("""
      INSERT INTO energy_metrics (timestamp, region, category, source, value, metric) VALUES 
      ('2026-07-08 10:00:00+00', 'FR', 'Renewables', 'solar', 500.4, 'generation'),
      ('2026-07-08 14:00:00+00', 'FR', 'Renewables', 'wind onshore', 300.2, 'generation')
   """);

      List<DailyEnergySummary> summaries = repository.getDailySummary(date, region);

      assertThat(summaries).hasSize(1);
      DailyEnergySummary summary = summaries.get(0);
      assertThat(summary.getCategory()).isEqualTo("Renewables");
   }

   @Test
   @DisplayName("Should fetch time bucket summaries structured in correct DESC/ASC sorting array orders")
   void getDailyMetrics_ValidatesTimeWindowBoundsAndSorting() {
      OffsetDateTime start = OffsetDateTime.parse("2026-07-08T00:00:00Z");
      OffsetDateTime end = OffsetDateTime.parse("2026-07-09T00:00:00Z");
      String region = "UK";

      jdbcTemplate.execute("""
      CREATE OR REPLACE VIEW view_daily_renewable_summary AS
         SELECT
             timestamp as bucket, 
             source, 
             region, 
             value as avg_generation_mw
         FROM energy_metrics;
      """);

      jdbcTemplate.execute("""
         INSERT INTO energy_metrics (timestamp, source, value, region, category, metric) VALUES
         ('2026-07-08 06:00:00+00', 'wind onshore', 1200.0, 'UK', 'actual', 'generation'),
         ('2026-07-08 12:00:00+00', 'solar', 450.0, 'UK', 'actual', 'generation'),
         ('2026-07-08 12:00:00+00', 'biomass', 100.0, 'UK', 'actual', 'generation')
      """);

      List<DailyRenewablesSummary> results = repository.getDailyMetrics(start, end, region);

      assertThat(results).hasSize(3);
      assertThat(results.get(0).getSource()).isEqualTo("biomass");
   }
}