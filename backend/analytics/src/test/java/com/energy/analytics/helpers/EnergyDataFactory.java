package com.energy.analytics.helpers;

import com.energy.analytics.model.EnergyMetric;
import com.energy.analytics.service.analytics.helpers.EnergySource;
import com.energy.analytics.service.analytics.helpers.EnergySourceMapper;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnergyDataFactory {

   public static List<EnergyMetric> createFullSnapshot(Instant ts, String region, double value) {
      return EnergySourceMapper.getAllRawSources().stream()
              .map(source -> new EnergyMetric(
                      ts,
                      region,
                      "generation",
                      source,
                      "actual aggregated",
                      value))
              .collect(Collectors.toList());
   }

   public static List<EnergyMetric> createCompleteBatch(Instant ts, double renewableVal, double fossilVal, double loadVal) {
      List<EnergyMetric> generationMetrics = new java.util.ArrayList<>(EnergySourceMapper.getAllRawSources().stream()
              .map(raw -> {
                 EnergySource source = EnergySourceMapper.from(raw);
                 double val = source.isRenewable() ? renewableVal : fossilVal;
                 return new EnergyMetric(
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
              new EnergyMetric(
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

   public static EnergyMetric create(String time, String source, double value) {
      return new EnergyMetric(
              Instant.parse(time),
              "DE_LU",
              "generation",
              source,
              "actual aggregated",
              value
      );
   }

   public static List<EnergyMetric> createFullSnapshot(String time, double baseValue) {
      return EnergySourceMapper.getAllRawSources().stream()
              .map(source -> create(time, source, baseValue))
              .toList();
   }

   public static List<EnergyMetric> createIncompleteBatch(Instant ts, double renewableVal, double fossilVal, double loadVal) {
      List<EnergyMetric> generationMetrics = createCompleteBatch(ts, renewableVal, fossilVal, loadVal);

      generationMetrics.removeFirst();

      return generationMetrics;
   }

   public static List<EnergyMetric> createBatchWithoutLoad(Instant ts, double renewableVal, double fossilVal, double loadVal) {
      List<EnergyMetric> generationMetrics = createCompleteBatch(ts, renewableVal, fossilVal, loadVal);

      return generationMetrics.stream()
              .filter(metric -> metric.getMetric().equals("load"))
              .toList();
   }

   @SafeVarargs
   public static List<EnergyMetric> combine(List<EnergyMetric>... lists) {
      return Stream.of(lists)
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
   }
}
