package com.energy.analytics.service.analytics;

import com.energy.analytics.event.CrossborderFlowsStoredEvent;
import com.energy.analytics.event.EnergyMetricsStoredEvent;
import com.energy.analytics.model.entity.FlowPoint;
import com.energy.analytics.model.projection.SourceContribution;
import com.energy.analytics.model.entity.DerivedMetric;
import com.energy.analytics.model.entity.Metric;
import com.energy.analytics.repository.CrossborderFlowRepository;
import com.energy.analytics.repository.DerivedMetricRepository;
import com.energy.analytics.repository.GridAnalyticsRepository;
import com.energy.analytics.service.analytics.util.SnapshotValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GridAnalyticsService {

   private final GridAnalyticsRepository gridAnalyticsRepository;
   private final DerivedMetricRepository derivedMetricRepository;
   private final CrossborderFlowRepository crossborderFlowRepository;

   private final WebSocketPublisher webSocketPublisher;

   @Async
   @EventListener
   public void onEnergyMetricsStored(EnergyMetricsStoredEvent event) {
      Instant ts = event.timestamp();
      String region = event.region();

      List<Metric> gridSnapshot = gridAnalyticsRepository.getGridSnapshot(ts, region);

      if (SnapshotValidator.isSnapshotComplete(gridSnapshot)) {
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

      List<FlowPoint> flowPoints = gridAnalyticsRepository.getFlowPoints(ts, region);

      if (!flowPoints.isEmpty()) {
         webSocketPublisher.sendFlowPoints(flowPoints, region);
      }

      crossborderFlowRepository.getFlowTotals(ts, region)
              .ifPresent(totals -> webSocketPublisher.sendFlowTotals(totals, region));
   }

   private List<DerivedMetric> calculateDerivedMetrics(Instant ts, String region, Collection<Metric> snapshot) {
      return MetricCalculatorRegistry.KPI_CALCULATORS.entrySet().stream()
              .map(entry -> new DerivedMetric(
                      ts,
                      region,
                      entry.getKey(),
                      entry.getValue().apply(snapshot)
              ))
              .toList();
   }

   public Map<String, List<SourceContribution>> calculateSourceContributions(Collection<Metric> snapshot) {
      return MetricCalculatorRegistry.TOP_SOURCES_CALCULATORS.entrySet().stream()
              .collect(Collectors.toMap(
                      Map.Entry::getKey,
                      e -> e.getValue().apply(snapshot)
              ));
   }

}
