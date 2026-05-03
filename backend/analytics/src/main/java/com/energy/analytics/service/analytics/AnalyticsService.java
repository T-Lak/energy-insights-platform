package com.energy.analytics.service.analytics;

import com.energy.analytics.model.EnergyMetric;
import com.energy.analytics.model.SlidingWindowKey;
import com.energy.analytics.service.analytics.helpers.EnergySource;
import com.energy.analytics.service.analytics.helpers.EnergySourceMapper;
import com.energy.analytics.service.state.GridCacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

   private final GridCacheStore gridCacheStore;

   private final AggregateCalculator aggregateCalculator;

   public void process(List<EnergyMetric> metrics) {
      Map<SlidingWindowKey, List<EnergyMetric>> groupedMetrics = metrics.stream()
              .collect(Collectors.groupingBy(EnergyMetric::toWindowKey));

      for (var entry : groupedMetrics.entrySet()) {
         List<EnergyMetric> sortedMetrics = entry.getValue().stream()
                 .sorted(Comparator.comparing(EnergyMetric::getTimestamp))
                 .toList();

         for (EnergyMetric metric : sortedMetrics) {
            Collection<EnergyMetric> window = gridCacheStore.updateSlidingWindow(entry.getKey(), metric);
            EnergyMetric smoothedMetric = computeIfReady(entry.getKey(), window, metric);

            if (smoothedMetric != null) {
               boolean changed = gridCacheStore.updateGridSnapshot(metric.getTimestamp(), smoothedMetric);

               if (!changed) continue;

               Collection<EnergyMetric> snapshot = gridCacheStore.getSnapshot(metric.getTimestamp());

               processGridSnapshots(metric.getTimestamp(), snapshot);
            }
         }
      }
   }

   private void processGridSnapshots(Instant timestamp, Collection<EnergyMetric> snapshot) {
      if (!isSnapshotComplete(snapshot)) {
         return;
      }

      double renewableShare = aggregateCalculator.calculateRenewableShare(snapshot);

      log.info("Renewable Share: {}", renewableShare);
   }

   private EnergyMetric computeIfReady(SlidingWindowKey key, Collection<EnergyMetric> window, EnergyMetric metric) {
      // TODO: consider time span rather than count/size
      if (window.size() < 3) {
         return null;
      }

      double avg = aggregateCalculator.calculateAverage(window);

      return new EnergyMetric(
              metric.getTimestamp(),
              metric.getRegion(),
              metric.getMetric(),
              metric.getSource(),
              metric.getCategory(),
              avg
      );
   }

   private boolean isSnapshotComplete(Collection<EnergyMetric> snapshot) {
      Set<EnergySource> presentSources = snapshot.stream()
              .filter(m -> m.getMetric().equals("generation"))
              .map(m -> EnergySourceMapper.from(m.getSource()))
              .collect(Collectors.toSet());

      Set<EnergySource> criticalSources = EnergySource.criticalGenerationSources();

      boolean containsAllCriticalSources = presentSources.containsAll(criticalSources);
      double generationCoverage = (double) presentSources.size() / EnergySource.values().length;

      boolean hasLoad = snapshot.stream()
              .anyMatch(m -> m.getMetric().equals("load"));

      return containsAllCriticalSources && hasLoad && generationCoverage > .8;
   }

}