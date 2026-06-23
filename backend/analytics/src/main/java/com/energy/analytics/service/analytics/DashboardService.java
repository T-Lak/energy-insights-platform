package com.energy.analytics.service.analytics;

import com.energy.analytics.dto.rest.KpiSnapshotPayload;
import com.energy.analytics.dto.rest.SourceRankingPayload;
import com.energy.analytics.dto.websocket.model.TimeseriesPointDTO;
import com.energy.analytics.model.domain.ContributionType;
import com.energy.analytics.model.entity.KpiSnapshotView;
import com.energy.analytics.model.entity.Metric;
import com.energy.analytics.model.projection.SourceContribution;
import com.energy.analytics.repository.DerivedMetricRepository;
import com.energy.analytics.repository.GridAnalyticsRepository;
import com.energy.analytics.service.analytics.util.SnapshotValidator;
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
   public KpiSnapshotPayload getLatestKpiSnapshot(String region) {
      log.info("fetching latest kpi data for region: {}", region);

      KpiSnapshotView snapshot = derivedMetricRepository.findLatestSnapshotByRegion(region)
              .orElseThrow(() -> new EntityNotFoundException("No KPI snapshots generated yet for region: " + region));

      Instant ts = snapshot.getBucket();

      log.info("Region: {}, Carbon Intensity: {}", snapshot.getRegion(), snapshot.getCarbonIntensity());

      return new KpiSnapshotPayload(
           new TimeseriesPointDTO(ts, snapshot.getRenewableShare()),
           new TimeseriesPointDTO(ts, snapshot.getCarbonIntensity()),
           new TimeseriesPointDTO(ts, snapshot.getTotalLoad()),
           new TimeseriesPointDTO(ts, snapshot.getNetBalance())
      );
   }

   public SourceRankingPayload getTopSources(String region) {
      log.info("fetching top sources data for region: {}", region);
      List<Metric> gridSnapshot = gridAnalyticsRepository.getGridSnapshot(Instant.now(), region);

      if (SnapshotValidator.isSnapshotComplete(gridSnapshot)) {
         Map<String, List<SourceContribution>> sourceContributions = gridAnalyticsService.
                 calculateSourceContributions(gridSnapshot);

         return new SourceRankingPayload(
              sourceContributions.get(ContributionType.TOP_EMERGY_SOURCES.getType()),
              sourceContributions.get(ContributionType.TOP_CARBON_CONTRIBUTORS.getType())
         );
      }

      throw new EntityNotFoundException("No grid snapshots found for region: " + region);
   }

}
