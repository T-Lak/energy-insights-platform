package com.energy.analytics.service.analytics;

import com.energy.analytics.model.entity.RawMetric;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AggregateCalculatorTest {

   private final AggregateCalculator calculator = new AggregateCalculator();

   @Test
   @DisplayName("Should return average value for a collection of metrics")
   void shouldCalculateAverage() {
      List<RawMetric> metrics = List.of(
              createMetric("solar", 100.0),
              createMetric("wind", 200.0)
      );

      double average = calculator.calculateAverage(metrics);

      assertThat(average).isEqualTo(150.0);
   }

   @Test
   @DisplayName("Should return 0.0 when calculating average of empty list")
   void shouldReturnZeroForEmptyAverage() {
      assertThat(calculator.calculateAverage(List.of())).isEqualTo(0.0);
   }

   @Test
   @DisplayName("Should calculate correct renewable share")
   void shouldCalculateRenewableShare() {
      List<RawMetric> metrics = List.of(
              createMetric("solar", 100.0),
              createMetric("fossil gas", 300.0),
              createMetric("load", 500.0, "actual consumption")
      );

      double share = calculator.calculateRenewableShare(metrics);

      assertThat(share).isEqualTo(0.25);
   }

   @Test
   @DisplayName("Should return 0.0 for renewable share when total production is zero")
   void shouldHandleDivisionByZeroForShare() {
      List<RawMetric> metrics = List.of(
              createMetric("solar", 0.0),
              createMetric("fossil gas", 0.0)
      );

      assertThat(calculator.calculateRenewableShare(metrics)).isEqualTo(0.0);
   }

   private RawMetric createMetric(String source, double value) {
      return createMetric(source, value, "actual aggregated");
   }

   private RawMetric createMetric(String source, double value, String category) {
      return new RawMetric(Instant.now(), "DE_LU", "generation", source, category, value);
   }
}
