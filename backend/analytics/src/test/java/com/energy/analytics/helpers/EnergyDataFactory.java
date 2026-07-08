package com.energy.analytics.helpers;

import com.energy.analytics.ingestion.model.Metric;
import com.energy.analytics.shared.domain.EnergySource;
import com.energy.analytics.shared.mapper.EnergySourceMapper;

import java.time.Instant;
import java.util.List;

public class EnergyDataFactory {

   public static List<Metric> createCompleteBatch(Instant ts, double renewableVal, double fossilVal, double loadVal) {
      List<Metric> generationMetrics = new java.util.ArrayList<>(EnergySourceMapper.getAllRawSources().stream()
              .map(raw -> {
                 EnergySource source = EnergySourceMapper.from(raw);
                 double val = source.isRenewable() ? renewableVal : fossilVal;
                 return new Metric(
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
              new Metric(
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

   public static Metric create(String time, String source, double value) {
      return new Metric(
              Instant.parse(time),
              "DE_LU",
              "generation",
              source,
              "actual aggregated",
              value
      );
   }

}
