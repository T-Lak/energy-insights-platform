package com.energy.analytics.service.analytics;

import com.energy.analytics.model.EnergyMetric;
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

}
