package com.energy.analytics.service.state;

import com.energy.analytics.model.entity.RawMetric;
import com.energy.analytics.model.keys.SnapshotKey;
import com.energy.analytics.model.keys.SlidingWindowKey;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GridCacheStore {

   private final Map<SlidingWindowKey, TreeMap<Instant, RawMetric>> slidingWindows = new ConcurrentHashMap<>();
   private final Map<Instant, Map<SnapshotKey, RawMetric>> gridSnapshots = new ConcurrentHashMap<>();

   private static final Duration SNAPSHOT_TTL = Duration.ofHours(2);

   public Collection<RawMetric> updateSlidingWindow(SlidingWindowKey key, RawMetric metric) {
      var window = slidingWindows.computeIfAbsent(key, k -> new TreeMap<>());

      window.put(metric.getTimestamp(), metric);

      Instant latestTs = window.lastKey();
      Instant cutoff = latestTs.minus(Duration.ofHours(1));
      window.headMap(cutoff).clear();

      return window.values();
   }

   public boolean updateGridSnapshot(Instant timestamp, RawMetric metric) {
      Map<SnapshotKey, RawMetric> snapshot = gridSnapshots
              .computeIfAbsent(timestamp, k -> new ConcurrentHashMap<>());

      SnapshotKey key = metric.toSnapshotKey();
      RawMetric existing = snapshot.get(key);

      if (existing != null) {
         double delta = Math.abs(existing.getValue() - metric.getValue());

         if (delta < 1e-6) return false;
      }

      snapshot.put(key, metric);
      return true;
   }

   public Collection<RawMetric> getSnapshot(Instant timestamp) {
      return gridSnapshots.getOrDefault(timestamp, Map.of()).values();
   }

   @Scheduled(fixedRate = 60000)
   public void cleanup() {
      Instant cutoff = Instant.now().minus(SNAPSHOT_TTL);
      gridSnapshots.keySet().removeIf(timestamp -> timestamp.isBefore(cutoff));
   }

}
