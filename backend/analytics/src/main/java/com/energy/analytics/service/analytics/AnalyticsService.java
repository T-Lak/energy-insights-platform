package com.energy.analytics.service.analytics;

import com.energy.analytics.model.EnergyMetric;
import com.energy.analytics.service.state.GridCacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

   private final GridCacheStore gridCacheStore;

   private final AggregateCalculator aggregateCalculator;

   private final Set<Instant> processedTimestamps = ConcurrentHashMap.newKeySet();

   public void process(List<EnergyMetric> metrics) {
      Map<String, List<EnergyMetric>> groupedMetrics = metrics.stream()
              .collect(Collectors.groupingBy(this::buildKey));

      for (var entry : groupedMetrics.entrySet()) {
         List<EnergyMetric> sortedMetrics = entry.getValue().stream()
                 .sorted(Comparator.comparing(EnergyMetric::getTimestamp))
                 .toList();

         for (EnergyMetric metric : sortedMetrics) {
            Collection<EnergyMetric> window = gridCacheStore.updateSlidingWindow(entry.getKey(), metric);
            EnergyMetric smoothedMetric = computeIfReady(entry.getKey(), window, metric);

            if (smoothedMetric != null) {
               Collection<EnergyMetric> snapshot = gridCacheStore
                       .updateGridSnapshot(metric.getTimestamp(), smoothedMetric);
               processGridSnapshotMetrics(metric.getTimestamp(), snapshot);
            }
         }
      }
   }

   private void processGridSnapshotMetrics(Instant timestamp, Collection<EnergyMetric> snapshot) {
      // TODO: processedTimestamps prevents value updates! change?
      if (!isComplete(snapshot) || !processedTimestamps.add(timestamp)) {
         return;
      }

      double renewableShare = aggregateCalculator.calculateRenewableShare(snapshot);

      log.info("Renewable Share: {}", renewableShare);
   }

   private EnergyMetric computeIfReady(String key, Collection<EnergyMetric> window, EnergyMetric metric) {
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

   private boolean isComplete(Collection<EnergyMetric> snapshot) {
      long generationCount = snapshot.stream()
              .filter(m -> m.getMetric().equals("generation"))
              .count();

      boolean hasLoad = snapshot.stream()
              .anyMatch(m -> m.getMetric().equals("load"));

      return generationCount >= 10 && hasLoad;
   }

   private String buildKey(EnergyMetric metric) {
      return metric.getRegion() + "|" + metric.getMetric() + "|" + metric.getSource() + "|" + metric.getCategory();
   }
}
