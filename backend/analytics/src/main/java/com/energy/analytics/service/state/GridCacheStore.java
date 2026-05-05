package com.energy.analytics.service.state;

import com.energy.analytics.model.EnergyMetric;
import com.energy.analytics.model.MetricKey;
import com.energy.analytics.model.SlidingWindowKey;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GridCacheStore {

   private final Map<SlidingWindowKey, TreeMap<Instant, EnergyMetric>> slidingWindows = new ConcurrentHashMap<>();
   private final Map<Instant, Map<MetricKey, EnergyMetric>> gridSnapshots = new ConcurrentHashMap<>();

   private static final Duration SNAPSHOT_TTL = Duration.ofHours(2);

   public Collection<EnergyMetric> updateSlidingWindow(SlidingWindowKey key, EnergyMetric metric) {
      var window = slidingWindows.computeIfAbsent(key, k -> new TreeMap<>());

      window.put(metric.getTimestamp(), metric);

      Instant latestTs = window.lastKey();
      Instant cutoff = latestTs.minus(Duration.ofHours(1));
      window.headMap(cutoff).clear();

      return window.values();
   }

   public boolean updateGridSnapshot(Instant timestamp, EnergyMetric metric) {
      Map<MetricKey, EnergyMetric> snapshot = gridSnapshots
              .computeIfAbsent(timestamp, k -> new ConcurrentHashMap<>());

      MetricKey key = metric.toSnapshotKey();
      EnergyMetric existing = snapshot.get(key);

      if (existing != null) {
         double delta = Math.abs(existing.getValue() - metric.getValue());

         if (delta < 1e-6) return false;
      }

      snapshot.put(key, metric);
      return true;
   }

   public Collection<EnergyMetric> getSnapshot(Instant timestamp) {
      return gridSnapshots.getOrDefault(timestamp, Map.of()).values();
   }

   @Scheduled(fixedRate = 60000)
   public void cleanup() {
      Instant cutoff = Instant.now().minus(SNAPSHOT_TTL);
      gridSnapshots.keySet().removeIf(timestamp -> timestamp.isBefore(cutoff));
   }

}
