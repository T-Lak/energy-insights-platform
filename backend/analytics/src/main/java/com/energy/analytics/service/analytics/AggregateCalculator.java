package com.energy.analytics.service.analytics;

import com.energy.analytics.model.entity.RawMetric;
import com.energy.analytics.model.mapper.EnergySourceMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class AggregateCalculator {

   public double calculateAverage(Collection<RawMetric> metrics) {
      return metrics.stream()
              .mapToDouble(RawMetric::getValue)
              .average()
              .orElse(0);
   }

   public double calculateRenewableShare(Collection<RawMetric> snapshot) {
      double renewable = snapshot.stream()
              .filter(this::isProduction)
              .filter(this::isRenewable)
              .mapToDouble(RawMetric::getValue)
              .sum();

      double total = snapshot.stream()
              .filter(this::isProduction)
              .mapToDouble(RawMetric::getValue)
              .sum();

      if (total == 0) {
         return 0.0;
      }

      return renewable / total;
   }

   private boolean isRenewable(RawMetric metric) {
      var source = EnergySourceMapper.from(metric.getSource());
      return source.isRenewable();
   }

   private boolean isProduction(RawMetric metric) {
      return metric.getCategory().equalsIgnoreCase("actual aggregated");
   }

}
