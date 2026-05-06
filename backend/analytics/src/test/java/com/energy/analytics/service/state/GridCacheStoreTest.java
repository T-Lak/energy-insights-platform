package com.energy.analytics.service.state;

import com.energy.analytics.helpers.EnergyDataFactory;
import com.energy.analytics.model.entity.RawMetric;
import com.energy.analytics.model.keys.SlidingWindowKey;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GridCacheStoreTest {


   private final GridCacheStore cacheStore = new GridCacheStore();

   static String region = "DE_LU";

   static Stream<Arguments> provideMetricsForFullHour() {
      String region = "DE_LU";
      Instant t0 = Instant.parse("2026-01-01T10:00:00Z");

      List<RawMetric> m0 = List.of(EnergyDataFactory.create(t0.toString(), "fossil gas", 100.3));
      List<RawMetric> m1 = List.of(EnergyDataFactory.create(t0.plus(Duration.ofMinutes(15)).toString(), "fossil gas", 57.0));
      List<RawMetric> m2 = List.of(EnergyDataFactory.create(t0.plus(Duration.ofMinutes(30)).toString(), "biomass", 174.1));

      List<RawMetric> m3 = List.of(new RawMetric(t0.plus(Duration.ofMinutes(45)), region, "generation", "biomass", "actual consumption", 174.1));

      return Stream.of(
              Arguments.of(EnergyDataFactory.combine(m0, m1, m2, m3))
      );
   }

   static Stream<Arguments> provideMetricsForMoreThanOneHour() {
      Instant t0 = Instant.parse("2026-01-01T10:00:00Z");

      List<RawMetric> m0 = EnergyDataFactory.createFullSnapshot(t0.toString(), 100.0);
      List<RawMetric> m1 = EnergyDataFactory.createFullSnapshot(t0.plus(Duration.ofMinutes(15)).toString(), 110.0);
      List<RawMetric> m2 = EnergyDataFactory.createFullSnapshot(t0.plus(Duration.ofMinutes(30)).toString(), 120.0);
      List<RawMetric> m3 = EnergyDataFactory.createFullSnapshot(t0.plus(Duration.ofMinutes(45)).toString(), 130.0);
      List<RawMetric> m4 = EnergyDataFactory.createFullSnapshot(t0.plus(Duration.ofHours(1)).toString(), 140.0);

      List<RawMetric> allMetrics = EnergyDataFactory.combine(m0, m1, m2, m3, m4);

      List<RawMetric> expectedWindow = EnergyDataFactory.combine(m1, m2, m3, m4);

      return Stream.of(
              Arguments.of(allMetrics, expectedWindow)
      );
   }

   static Stream<Arguments> provideMetricsForSnapshots() {
      String t0 = "2026-01-01T10:00:00Z";
      String source = "fossil gas";

      return Stream.of(
              // Case 1: Actual change -> Expected: true
              Arguments.of(List.of(
                      EnergyDataFactory.create(t0, source, 100.3),
                      EnergyDataFactory.create(t0, source, 57.0)
              ), true),

              // Case 2: Identical values -> Expected: false
              Arguments.of(List.of(
                      EnergyDataFactory.create(t0, source, 100.3),
                      EnergyDataFactory.create(t0, source, 100.3)
              ), false),

              // Case 3: Outside Epsilon (1e-6) -> Expected: true
              Arguments.of(List.of(
                      EnergyDataFactory.create(t0, source, 100.000003),
                      EnergyDataFactory.create(t0, source, 100.000002)
              ), true),

              // Case 4: Inside Epsilon -> Expected: false
              Arguments.of(List.of(
                      EnergyDataFactory.create(t0, source, 100.0000003),
                      EnergyDataFactory.create(t0, source, 100.0000002)
              ), false)
      );
   }

   @ParameterizedTest
   @MethodSource("provideMetricsForFullHour")
   void shouldStoreAndReturnFullWindow(List<RawMetric> metrics) {
      List<RawMetric> processedMetrics = new ArrayList<>();

      for (RawMetric metric : metrics) {
         processedMetrics.add(metric);
         SlidingWindowKey key = metric.toWindowKey();
         Collection<RawMetric> window = cacheStore.updateSlidingWindow(key, metric);

         assertThat(window.stream().toList().containsAll(processedMetrics));
      }
   }

   @ParameterizedTest
   @MethodSource("provideMetricsForMoreThanOneHour")
   void shouldReturnMetricsOfLastHour(List<RawMetric> metrics, List<RawMetric> expected) {
      for (int i = 0; i < metrics.size()-1; i++) {
         RawMetric metric = metrics.get(i);
         SlidingWindowKey key = metric.toWindowKey();
         cacheStore.updateSlidingWindow(key, metric);
      }

      RawMetric lastMetric = metrics.getLast();
      SlidingWindowKey key = lastMetric.toWindowKey();
      Collection<RawMetric> window = cacheStore.updateSlidingWindow(key, lastMetric);

      assertThat(window.containsAll(expected));
   }

   @ParameterizedTest
   @MethodSource("provideMetricsForSnapshots")
   void shouldReturnCorrectBoolean(List<RawMetric> metrics, boolean expected) {
      cacheStore.updateGridSnapshot(metrics.getFirst().getTimestamp(), metrics.getFirst());
      assertThat(expected)
              .isEqualTo(cacheStore.updateGridSnapshot(metrics.getLast().getTimestamp(), metrics.getLast()));
   }

}
