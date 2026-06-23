package com.energy.analytics.service.analytics.util;

import com.energy.analytics.model.domain.EnergySource;
import com.energy.analytics.model.entity.Metric;
import com.energy.analytics.model.mapper.EnergySourceMapper;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public final class SnapshotValidator {

   private SnapshotValidator() {
      throw new UnsupportedOperationException("Utility class");
   }

   public static boolean isSnapshotComplete(Collection<Metric> snapshot) {
      Set<EnergySource> presentSources = snapshot.stream()
              .filter(m -> "generation".equalsIgnoreCase(m.getMetric()))
              .map(m -> EnergySourceMapper.from(m.getSource()))
              .collect(Collectors.toSet());

      Set<EnergySource> criticalSources = EnergySource.criticalGenerationSources();

      boolean containsAllCriticalSources = presentSources.containsAll(criticalSources);
      double generationCoverage = (double) presentSources.size() / EnergySource.values().length;

      boolean hasLoad = snapshot.stream()
              .anyMatch(m -> "load".equalsIgnoreCase(m.getMetric()));

      return containsAllCriticalSources && hasLoad && (generationCoverage > .8);
   }
}
