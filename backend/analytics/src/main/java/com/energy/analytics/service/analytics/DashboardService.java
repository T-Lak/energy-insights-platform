package com.energy.analytics.service.analytics;

import com.energy.analytics.dto.rest.KpiSnapshotPayload;
import com.energy.analytics.dto.websocket.model.TimeseriesPointDTO;
import com.energy.analytics.model.entity.KpiSnapshotView;
import com.energy.analytics.repository.DerivedMetricRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardService {

   private final DerivedMetricRepository derivedMetricRepository;

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

}
