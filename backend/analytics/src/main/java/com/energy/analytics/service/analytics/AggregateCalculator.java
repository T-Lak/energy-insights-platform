package com.energy.analytics.service.analytics;

import com.energy.analytics.model.EnergyMetric;
import com.energy.analytics.service.analytics.helpers.EnergySourceMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class AggregateCalculator {

   public double calculateAverage(Collection<EnergyMetric> metrics) {
      return metrics.stream()
              .mapToDouble(EnergyMetric::getValue)
              .average()
              .orElse(0);
   }

   public double calculateRenewableShare(Collection<EnergyMetric> snapshot) {
      double renewable = snapshot.stream()
              .filter(this::isProduction)
              .filter(this::isRenewable)
              .mapToDouble(EnergyMetric::getValue)
              .sum();

      double total = snapshot.stream()
              .filter(this::isProduction)
              .mapToDouble(EnergyMetric::getValue)
              .sum();

      if (total == 0) {
         return 0.0;
      }

      return renewable / total;
   }

   private boolean isRenewable(EnergyMetric metric) {
      var source = EnergySourceMapper.from(metric.getSource());
      return source.isRenewable();
   }

   private boolean isProduction(EnergyMetric metric) {
      return metric.getCategory().equalsIgnoreCase("actual aggregated");
   }

}
