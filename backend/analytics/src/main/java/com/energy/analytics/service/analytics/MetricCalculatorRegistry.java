package com.energy.analytics.service.analytics;

import com.energy.analytics.model.projection.SourceContribution;
import com.energy.analytics.model.entity.Metric;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Getter
@Component
public class MetricCalculatorRegistry {

   private final Map<String, Function<Collection<Metric>, Double>> kpiCalculators;
   private final Map<String, Function<Collection<Metric>, List<SourceContribution>>> topSourcesCalculators;

   public MetricCalculatorRegistry(AggregateCalculator aggregateCalculator) {
      this.kpiCalculators = Map.of(
           "renewable_share", aggregateCalculator::calculateRenewableShare,
           "carbon_intensity", aggregateCalculator::calculateCarbonIntensity,
           "total_load", aggregateCalculator::calculateTotalLoad,
           "net_balance", aggregateCalculator::calculateNetBalance
      );

      this.topSourcesCalculators = Map.of (
        "top_energy_sources", aggregateCalculator::calculateTopEnergySources,
        "top_carbon_contributors", aggregateCalculator::calculateTopCarbonContributors
      );
   }

}
