package com.energy.analytics.dashboard;

import com.energy.analytics.dashboard.dto.DashboardSummarySnapshot;
import com.energy.analytics.dashboard.dto.DashboardLeaderboardOverview;
import com.energy.analytics.dashboard.dto.TimeseriesPointDTO;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardService {

   private final GridAnalyticsService gridAnalyticsService;

   private final DerivedMetricRepository derivedMetricRepository;
   private final GridAnalyticsRepository gridAnalyticsRepository;

   @Transactional
   public DashboardSummarySnapshot getLatestKpiSnapshot(String region) {
      log.info("fetching latest kpi data for region: {}", region);

      KpiSnapshotView snapshot = derivedMetricRepository.findLatestSnapshotByRegion(region)
              .orElseThrow(() -> new EntityNotFoundException("No KPI snapshots generated yet for region: " + region));

      Instant ts = snapshot.getBucket();

      log.info("Region: {}, Carbon Intensity: {}", snapshot.getRegion(), snapshot.getCarbonIntensity());

      return new DashboardSummarySnapshot(
           new TimeseriesPointDTO(ts, snapshot.getRenewableShare()),
           new TimeseriesPointDTO(ts, snapshot.getCarbonIntensity()),
           new TimeseriesPointDTO(ts, snapshot.getTotalLoad()),
           new TimeseriesPointDTO(ts, snapshot.getNetBalance())
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

}
