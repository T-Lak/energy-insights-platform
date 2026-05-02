package com.energy.analytics.service.state;

import com.energy.analytics.model.EnergyMetric;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GridCacheStore {

   private final Map<String, TreeMap<Instant, EnergyMetric>> slidingWindows = new ConcurrentHashMap<>();
   private final Map<Instant, Set<EnergyMetric>> gridSnapshots = new ConcurrentHashMap<>();

   private static final Duration SNAPSHOT_TTL = Duration.ofHours(2);

   public Collection<EnergyMetric> updateSlidingWindow(String key, EnergyMetric metric) {
      var window = slidingWindows.computeIfAbsent(key, k -> new TreeMap<>());

      window.put(metric.getTimestamp(), metric);

      Instant latestTs = window.lastKey();
      Instant cutoff = latestTs.minus(Duration.ofHours(1));
      window.headMap(cutoff).clear();

      return window.values();
   }

   public Collection<EnergyMetric> updateGridSnapshot(Instant timestamp, EnergyMetric metric) {
      Set<EnergyMetric> snapshot = gridSnapshots.computeIfAbsent(timestamp, k -> new HashSet<>());

      snapshot.removeIf(m ->
             m.getMetric().equals(metric.getMetric()) &&
             m.getSource().equals(metric.getSource()) &&
             m.getCategory().equals(metric.getCategory())
      );

      snapshot.add(metric);

      return snapshot;
   }

   @Scheduled(fixedRate = 60000)
   public void cleanup() {
      Instant cutoff = Instant.now().minus(SNAPSHOT_TTL);
      gridSnapshots.keySet().removeIf(ts -> ts.isBefore(cutoff));
   }

}
