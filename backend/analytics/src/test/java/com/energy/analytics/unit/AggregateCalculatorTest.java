package com.energy.analytics.unit;

import com.energy.analytics.ingestion.model.Metric;
import com.energy.analytics.crossborder.model.projection.SourceContribution;
import com.energy.analytics.shared.calculation.AggregateCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class AggregateCalculatorTest {

   private Metric createMetric(String metric, String category, String source, double value) {
      Metric m = new Metric();
      m.setMetric(metric);
      m.setCategory(category);
      m.setSource(source);
      m.setValue(value);
      return m;
   }

   @Nested
   @DisplayName("Renewable Share Calculations")
   class RenewableShareTests {

      @Test
      @DisplayName("Should correctly calculate the renewable percentage share")
      void calculateRenewableShare_Success() {
         List<Metric> snapshot = List.of(
                 createMetric("generation", "actual aggregated", "solar", 40.0),
                 createMetric("generation", "actual aggregated", "wind onshore", 20.0),
                 createMetric("generation", "actual aggregated", "fossil gas", 40.0)
         );

         double share = AggregateCalculator.calculateRenewableShare(snapshot);

         assertThat(share).isEqualTo(0.60, within(0.001));
      }

      @Test
      @DisplayName("Should return 0.0 when total generation is zero to prevent NaN")
      void calculateRenewableShare_ZeroGeneration() {
         List<Metric> snapshot = List.of(
                 createMetric("generation", "actual aggregated", "SOLAR", 0.0)
         );

         double share = AggregateCalculator.calculateRenewableShare(snapshot);
         assertThat(share).isEqualTo(0.0);
      }

      @Test
      @DisplayName("Should ignore non-production category types")
      void calculateRenewableShare_IgnoresForecasts() {
         List<Metric> snapshot = List.of(
                 createMetric("generation", "actual aggregated", "SOLAR", 50.0),
                 createMetric("generation", "forecasted day ahead", "WIND_ONSHORE", 50.0)
         );

         double share = AggregateCalculator.calculateRenewableShare(snapshot);
         assertThat(share).isEqualTo(1.0);
      }
   }

   @Nested
   @DisplayName("Carbon Intensity & Load Calculations")
   class CarbonAndLoadTests {

      @Test
      @DisplayName("Should calculate total load from load metrics")
      void calculateTotalLoad_Success() {
         List<Metric> snapshot = List.of(
                 createMetric("load", "actual aggregated", "load", 55.4),
                 createMetric("generation", "actual aggregated", "SOLAR", 20.0)
         );

         double load = AggregateCalculator.calculateTotalLoad(snapshot);
         assertThat(load).isEqualTo(55.4);
      }

      @Test
      @DisplayName("Should return 0.0 for load if metric is missing")
      void calculateTotalLoad_Missing() {
         assertThat(AggregateCalculator.calculateTotalLoad(Collections.emptyList())).isEqualTo(0.0);
      }

      @Test
      @DisplayName("Should calculate net balance as generation minus load")
      void calculateNetBalance_Success() {
         List<Metric> snapshot = List.of(
                 createMetric("generation", "actual aggregated", "solar", 100.0),
                 createMetric("load", "actual", "load", 80.0)
         );

         double balance = AggregateCalculator.calculateNetBalance(snapshot);
         assertThat(balance).isEqualTo(20.0);
      }
   }

   @Nested
   @DisplayName("Top Contributors Parsing")
   class TopContributorsTests {

      @Test
      @DisplayName("Should aggregate and limit output to the top 3 highest energy sources")
      void calculateTopEnergySources_FiltersAndSortsTopThree() {
         List<Metric> snapshot = List.of(
                 createMetric("generation", "actual aggregated", "SOLAR", 10.0),
                 createMetric("generation", "actual aggregated", "WIND_ONSHORE", 50.0),
                 createMetric("generation", "actual aggregated", "FOSSIL_GAS", 30.0),
                 createMetric("generation", "actual aggregated", "BIOMASS", 5.0)
         );

         List<SourceContribution> topSources = AggregateCalculator.calculateTopEnergySources(snapshot);

         assertThat(topSources).hasSize(3);
         assertThat(topSources.get(0).source()).isEqualTo("WIND_ONSHORE");
         assertThat(topSources.get(0).value()).isEqualTo(50.0);

         assertThat(topSources.get(1).source()).isEqualTo("FOSSIL_GAS");
         assertThat(topSources.get(2).source()).isEqualTo("SOLAR");

         assertThat(topSources).extracting(SourceContribution::source)
                 .doesNotContain("BIOMASS");
      }
   }
}