package com.energy.analytics.service.analytics;

import com.energy.analytics.model.domain.EmissionCategory;
import com.energy.analytics.model.domain.EnergySource;
import com.energy.analytics.model.projection.SourceContribution;
import com.energy.analytics.model.entity.Metric;
import com.energy.analytics.model.mapper.EmissionFactorMapper;
import com.energy.analytics.model.mapper.EnergySourceMapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class AggregateCalculator {

   private AggregateCalculator() {}

   public static double calculateRenewableShare(Collection<Metric> snapshot) {
      double renewable = snapshot.stream()
              .filter(AggregateCalculator::isProductionMetric)
              .filter(AggregateCalculator::isRenewable)
              .mapToDouble(Metric::getValue)
              .sum();

      double totalGeneration = calculateTotalGeneration(snapshot);

      if (totalGeneration == 0) {
         return 0.0;
      }

      return renewable / totalGeneration;
   }

   public static  double calculateCarbonIntensity(Collection<Metric> snapshot) {
      double totalEmissions = 0.0;
      double totalGeneration = 0.0;

      for (Metric m : snapshot) {
         if (!isProductionMetric(m)) continue;

         EnergySource source = EnergySourceMapper.from(m.getSource());
         EmissionCategory category = EmissionFactorMapper.from(source);

         if (category == EmissionCategory.UNKNOWN) {
            continue;
         }

         double value = m.getValue();

         totalGeneration += value;
         totalEmissions += value * category.getEmissionFactor();
      }

      return totalGeneration == 0 ? 0.0 : totalEmissions / totalGeneration;
   }

   public static  double calculateTotalLoad(Collection<Metric> snapshot) {
      return snapshot.stream()
              .filter(m -> "load".equalsIgnoreCase(m.getMetric()))
              .findFirst()
              .map(Metric::getValue)
              .orElse(0.0);
   }

   public static  double calculateNetBalance(Collection<Metric> snapshot) {
      double totalGeneration = AggregateCalculator.calculateTotalGeneration(snapshot);
      double totalLoad = AggregateCalculator.calculateTotalLoad(snapshot);

      return totalGeneration - totalLoad;
   }

   public static  List<SourceContribution> calculateTopEnergySources(Collection<Metric> snapshot) {
      Map<String, Double> totals = snapshot.stream()
              .filter(AggregateCalculator::isProductionMetric)
              .filter(m -> "generation".equalsIgnoreCase(m.getMetric()))
              .collect(Collectors.groupingBy(
                      Metric::getSource,
                      Collectors.summingDouble(Metric::getValue)
              ));

      return totals.entrySet().stream()
              .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
              .limit(3)
              .map(e -> new SourceContribution(e.getKey(), e.getValue()))
              .toList();
   }

   public static  List<SourceContribution> calculateTopCarbonContributors(Collection<Metric> snapshot) {
      Map<String, Double> emissions = new HashMap<>();

      for (Metric m : snapshot) {
         if (!isProductionMetric(m)) continue;

         EnergySource source = EnergySourceMapper.from(m.getSource());
         EmissionCategory category = EmissionFactorMapper.from(source);

         if (category == EmissionCategory.UNKNOWN) continue;

         double contribution = m.getValue() * category.getEmissionFactor();
         emissions.merge(m.getSource(), contribution, Double::sum);
      }

      return emissions.entrySet().stream()
              .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
              .limit(3)
              .map(e -> new SourceContribution(e.getKey(), e.getValue()))
              .toList();
   }

   private static  double calculateTotalGeneration(Collection<Metric> snapshot) {
      return snapshot.stream()
              .filter(AggregateCalculator::isProductionMetric)
              .mapToDouble(Metric::getValue)
              .sum();
   }

   private static  boolean isRenewable(Metric metric) {
      var source = EnergySourceMapper.from(metric.getSource());
      return source.isRenewable();
   }

   private static boolean isProductionMetric(Metric metric) {
      return metric.getCategory().equalsIgnoreCase("actual aggregated");
   }

}
