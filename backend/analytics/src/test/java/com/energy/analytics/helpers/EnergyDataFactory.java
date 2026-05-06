package com.energy.analytics.helpers;

import com.energy.analytics.model.entity.RawMetric;
import com.energy.analytics.model.domain.EnergySource;
import com.energy.analytics.model.mapper.EnergySourceMapper;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnergyDataFactory {

   public static List<RawMetric> createFullSnapshot(Instant ts, String region, double value) {
      return EnergySourceMapper.getAllRawSources().stream()
              .map(source -> new RawMetric(
                      ts,
                      region,
                      "generation",
                      source,
                      "actual aggregated",
                      value))
              .collect(Collectors.toList());
   }

   public static List<RawMetric> createCompleteBatch(Instant ts, double renewableVal, double fossilVal, double loadVal) {
      List<RawMetric> generationMetrics = new java.util.ArrayList<>(EnergySourceMapper.getAllRawSources().stream()
              .map(raw -> {
                 EnergySource source = EnergySourceMapper.from(raw);
                 double val = source.isRenewable() ? renewableVal : fossilVal;
                 return new RawMetric(
                         ts,
                         "DE_LU",
                         "generation",
                         source.toString(),
                         "actual",
                         val
                 );
              })
              .toList());

      generationMetrics.add(
              new RawMetric(
                      ts,
                      "DE_LU",
                      "load",
                      "load actual",
                      "actual",
                      loadVal
              )
      );

      return generationMetrics;
   }

   public static RawMetric create(String time, String source, double value) {
      return new RawMetric(
              Instant.parse(time),
              "DE_LU",
              "generation",
              source,
              "actual aggregated",
              value
      );
   }

   public static List<RawMetric> createFullSnapshot(String time, double baseValue) {
      return EnergySourceMapper.getAllRawSources().stream()
              .map(source -> create(time, source, baseValue))
              .toList();
   }

   public static List<RawMetric> createIncompleteBatch(Instant ts, double renewableVal, double fossilVal, double loadVal) {
      List<RawMetric> generationMetrics = createCompleteBatch(ts, renewableVal, fossilVal, loadVal);

      generationMetrics.removeFirst();

      return generationMetrics;
   }

   public static List<RawMetric> createBatchWithoutLoad(Instant ts, double renewableVal, double fossilVal, double loadVal) {
      List<RawMetric> generationMetrics = createCompleteBatch(ts, renewableVal, fossilVal, loadVal);

      return generationMetrics.stream()
              .filter(metric -> metric.getMetric().equals("load"))
              .toList();
   }

   @SafeVarargs
   public static List<RawMetric> combine(List<RawMetric>... lists) {
      return Stream.of(lists)
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
   }
}
