package com.energy.analytics.service.analytics;

import com.energy.analytics.dto.rest.CrossborderFlowTotalsTsPayload;
import com.energy.analytics.dto.rest.LatestFlowsPayload;
import com.energy.analytics.dto.websocket.model.FlowPointDTO;
import com.energy.analytics.dto.websocket.model.FlowTotalsDTO;
import com.energy.analytics.repository.CrossborderFlowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrossborderFlowService {

   private final CrossborderFlowRepository crossborderFlowRepository;

   public CrossborderFlowTotalsTsPayload getFlowTotalsTimeSeries(int hours, String region) {
      if (hours <= 0 || hours > 24) {
         throw new IllegalArgumentException("hours must be between 1 and 24");
      }

      log.info("fetching latest flow timeseries data for region: {}", region);

      Instant end = Instant.now();
      Instant start = end.minus(hours, ChronoUnit.HOURS);

      List<FlowTotalsDTO> flowTotals = crossborderFlowRepository.getFlowTotals(start, end, region);

      return new CrossborderFlowTotalsTsPayload(region, Instant.now(), flowTotals);
   }

   public LatestFlowsPayload getLatestFlowPoints(String region) {
      log.info("Fetching latest flow points for region: {}", region);

      List<FlowPointDTO> flowPoints = crossborderFlowRepository.getLatestFlowPoints(region);

      if (flowPoints == null) {
         flowPoints = Collections.emptyList();
      }

      return new LatestFlowsPayload(flowPoints);
   }
}
