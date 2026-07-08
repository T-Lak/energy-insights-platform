package com.energy.analytics.shared.calculation;

import com.energy.analytics.shared.domain.ContributionType;
import com.energy.analytics.shared.domain.MetricKey;
import com.energy.analytics.crossborder.model.projection.SourceContribution;
import com.energy.analytics.ingestion.model.Metric;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class MetricCalculatorRegistry {

   private MetricCalculatorRegistry() {}

   public static final Map<String, Function<Collection<Metric>, Double>> KPI_CALCULATORS = Map.of(
           MetricKey.RENEWABLE_SHARE.getKey(), AggregateCalculator::calculateRenewableShare,
           MetricKey.CARBON_INTENSITY.getKey(), AggregateCalculator::calculateCarbonIntensity,
           MetricKey.TOTAL_LOAD.getKey(), AggregateCalculator::calculateTotalLoad,
           MetricKey.NET_BALANCE.getKey(), AggregateCalculator::calculateNetBalance
   );

   public static final Map<String, Function<Collection<Metric>, List<SourceContribution>>> TOP_SOURCES_CALCULATORS = Map.of(
           ContributionType.TOP_EMERGY_SOURCES.getType(), AggregateCalculator::calculateTopEnergySources,
           ContributionType.TOP_CARBON_CONTRIBUTORS.getType(), AggregateCalculator::calculateTopCarbonContributors
   );
}
