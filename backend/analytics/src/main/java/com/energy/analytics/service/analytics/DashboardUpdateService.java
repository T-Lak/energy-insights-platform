package com.energy.analytics.service.analytics;

import com.energy.analytics.event.CrossborderFlowsStoredEvent;
import com.energy.analytics.event.EnergyMetricsStoredEvent;
import com.energy.analytics.model.domain.EnergySource;
import com.energy.analytics.model.projection.SourceContribution;
import com.energy.analytics.model.entity.DerivedMetric;
import com.energy.analytics.model.entity.Metric;
import com.energy.analytics.model.mapper.EnergySourceMapper;
import com.energy.analytics.repository.DerivedMetricRepository;
import com.energy.analytics.repository.EnergyAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardUpdateService {

   private final EnergyAnalyticsRepository energyAnalyticsRepository;
   private final DerivedMetricRepository derivedMetricRepository;

   private final MetricCalculatorRegistry calculatorRegistry;

   private final DashboardWebSocketPublisher webSocketPublisher;

   @Async
   @EventListener
   public void onEnergyMetricsStored(EnergyMetricsStoredEvent event) {
      Instant ts = event.timestamp();
      String region = event.region();

      List<Metric> gridSnapshot = energyAnalyticsRepository.getGridSnapshot(ts, region);

      if (isSnapshotComplete(gridSnapshot)) {
         List<DerivedMetric> derivedMetrics = calculateDerivedMetrics(ts, region, gridSnapshot);
         Map<String, List<SourceContribution>> sourceContributions = calculateSourceContributions(gridSnapshot);

         if (!derivedMetrics.isEmpty()) {
            derivedMetricRepository.upsertBatch(derivedMetrics);
            webSocketPublisher.sendKPIs(derivedMetrics, region);
         }

         if (!sourceContributions.isEmpty()) {
            webSocketPublisher.sendSourceContributions(sourceContributions, region);
         }
      }
   }

   @Async
   @EventListener
   public void onCrossborderFlowsStored(CrossborderFlowsStoredEvent event) {
      Instant ts = event.timestamp();
      String region = event.region();
   }

   private List<DerivedMetric> calculateDerivedMetrics(Instant ts, String region, Collection<Metric> snapshot) {
      return calculatorRegistry.getKpiCalculators().entrySet().stream()
              .map(entry -> new DerivedMetric(
                      ts,
                      region,
                      entry.getKey(),
                      entry.getValue().apply(snapshot)
              ))
              .toList();
   }

   private Map<String, List<SourceContribution>> calculateSourceContributions(Collection<Metric> snapshot) {
      return calculatorRegistry.getTopSourcesCalculators().entrySet().stream()
              .collect(Collectors.toMap(
                      Map.Entry::getKey,
                      e -> e.getValue().apply(snapshot)
              ));
   }

   private boolean isSnapshotComplete(Collection<Metric> snapshot) {
      Set<EnergySource> presentSources = snapshot.stream()
              .filter(m -> m.getMetric().equals("generation"))
              .map(m -> EnergySourceMapper.from(m.getSource()))
              .collect(Collectors.toSet());

      Set<EnergySource> criticalSources = EnergySource.criticalGenerationSources();

      boolean containsAllCriticalSources = presentSources.containsAll(criticalSources);
      double generationCoverage = (double) presentSources.size() / EnergySource.values().length;

      boolean hasLoad = snapshot.stream()
              .anyMatch(m -> m.getMetric().equals("load"));

      return containsAllCriticalSources && hasLoad && (generationCoverage > .8);
   }

}
