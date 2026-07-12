package com.energy.analytics.dashboard;

import com.energy.analytics.dashboard.dto.DashboardKpiPointDTO;
import com.energy.analytics.dashboard.dto.DashboardSummarySnapshot;
import com.energy.analytics.dashboard.dto.DashboardLeaderboardOverview;
import com.energy.analytics.shared.domain.ContributionType;
import com.energy.analytics.dashboard.model.KpiSnapshotView;
import com.energy.analytics.ingestion.model.Metric;
import com.energy.analytics.crossborder.model.projection.SourceContribution;
import com.energy.analytics.dashboard.repository.DerivedMetricRepository;
import com.energy.analytics.dashboard.repository.GridAnalyticsRepository;
import com.energy.analytics.shared.calculation.util.SnapshotValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardService {

   private final GridAnalyticsService gridAnalyticsService;

   private final DerivedMetricRepository derivedMetricRepository;
   private final GridAnalyticsRepository gridAnalyticsRepository;

   @Transactional
   public DashboardSummarySnapshot getLatestKpiSnapshot(String region) {
      List<KpiSnapshotView> snapshots = derivedMetricRepository.findLatestSnapshotsByRegion(region);

      if (snapshots.isEmpty()) {
         throw new EntityNotFoundException("No data for region: " + region);
      }

      KpiSnapshotView current = snapshots.get(0);
      KpiSnapshotView previous = (snapshots.size() > 1) ? snapshots.get(1) : null;

      return new DashboardSummarySnapshot(
              mapToDto(current, previous, KpiSnapshotView::getRenewableShare),
              mapToDto(current, previous, KpiSnapshotView::getCarbonIntensity),
              mapToDto(current, previous, KpiSnapshotView::getTotalLoad),
              mapToDto(current, previous, KpiSnapshotView::getNetBalance)
      );
   }

   public DashboardLeaderboardOverview getTopSources(String region) {
      log.info("fetching top sources data for region: {}", region);
      List<Metric> gridSnapshot = gridAnalyticsRepository.getGridSnapshot(Instant.now(), region);

      if (SnapshotValidator.isSnapshotComplete(gridSnapshot)) {
         Map<String, List<SourceContribution>> sourceContributions = gridAnalyticsService.
                 calculateSourceContributions(gridSnapshot);

         return new DashboardLeaderboardOverview(
              sourceContributions.get(ContributionType.TOP_EMERGY_SOURCES.getType()),
              sourceContributions.get(ContributionType.TOP_CARBON_CONTRIBUTORS.getType())
         );
      }

      throw new EntityNotFoundException("No grid snapshots found for region: " + region);
   }

   private DashboardKpiPointDTO mapToDto(
           KpiSnapshotView current,
           KpiSnapshotView previous,
           Function<KpiSnapshotView, Double> extractor
   ) {
      Double currVal = extractor.apply(current);
      Double prevVal = (previous != null) ? extractor.apply(previous) : null;

      Double delta = (prevVal != null && prevVal != 0) ? ((currVal - prevVal) / prevVal) * 100 : 0.0;

      return new DashboardKpiPointDTO(current.getBucket(), currVal, delta);
   }

}
