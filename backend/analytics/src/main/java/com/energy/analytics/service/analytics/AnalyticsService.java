package com.energy.analytics.service.analytics;

import com.energy.analytics.model.entity.DerivedMetric;
import com.energy.analytics.model.entity.RawMetric;
import com.energy.analytics.model.keys.SlidingWindowKey;
import com.energy.analytics.repository.DerivedMetricRepositoryImpl;
import com.energy.analytics.repository.SmoothedMetricRepositoryImpl;
import com.energy.analytics.model.domain.EnergySource;
import com.energy.analytics.model.mapper.EnergySourceMapper;
import com.energy.analytics.service.state.GridCacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

   private final SmoothedMetricRepositoryImpl smoothedMetricRepository;
   private final DerivedMetricRepositoryImpl derivedMetricRepository;

   private SimpMessagingTemplate messagingTemplate;

   private final GridCacheStore gridCacheStore;

   private final AggregateCalculator aggregateCalculator;

   public void process(List<RawMetric> metrics) {
      Set<Instant> modifiedTimestamps = new HashSet<>();

      List<RawMetric> smoothedMetrics = new ArrayList<>();
      List<DerivedMetric> derivedMetrics = new ArrayList<>();

      for (RawMetric metric : metrics) {
         SlidingWindowKey key = metric.toWindowKey();
         Collection<RawMetric> window = gridCacheStore.updateSlidingWindow(key, metric);
         RawMetric smoothedMetric = computeIfReady(window, metric);

         if (smoothedMetric != null) {
            smoothedMetrics.add(smoothedMetric);

            boolean changed = gridCacheStore.updateGridSnapshot(metric.getTimestamp(), smoothedMetric);
            if (changed) modifiedTimestamps.add(metric.getTimestamp());

         }
      }

      if (!smoothedMetrics.isEmpty()) smoothedMetricRepository.upsertBatch(smoothedMetrics);

      for (Instant ts : modifiedTimestamps) {
         DerivedMetric derivedMetric = processGridSnapshots(ts, List.copyOf(gridCacheStore.getSnapshot(ts)));
         if (derivedMetric != null) {
            derivedMetrics.add(derivedMetric);
//            messagingTemplate.convertAndSend("/topic/renewable-share");
//            log.info("Broadcasted metric for {}: {}%", derivedMetric.getTimestamp(), derivedMetric.getValue() * 100);
         }
      }

      if (!derivedMetrics.isEmpty()) derivedMetricRepository.upsertBatch(derivedMetrics);
   }

   private DerivedMetric processGridSnapshots(Instant timestamp, Collection<RawMetric> snapshot) {
      if (!isSnapshotComplete(snapshot)) return null;

      String region = snapshot.stream().findFirst().map(RawMetric::getRegion).orElse("UNKNOWN");

      double renewableShare = aggregateCalculator.calculateRenewableShare(snapshot);

      log.info("Region: {} | Time: {} | Renewable Share: {}%",
              region,
              timestamp,
              String.format("%.2f", renewableShare * 100));

      return new DerivedMetric(timestamp, region, "renewable share", renewableShare);
   }

   private RawMetric computeIfReady(Collection<RawMetric> window, RawMetric metric) {
      // TODO: consider time span rather than count/size
      if (window.size() < 3) return null;

      double avg = aggregateCalculator.calculateAverage(window);

      return new RawMetric(
           metric.getTimestamp(),
           metric.getRegion(),
           metric.getMetric(),
           metric.getSource(),
           metric.getCategory(),
           avg
      );
   }

   private boolean isSnapshotComplete(Collection<RawMetric> snapshot) {
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