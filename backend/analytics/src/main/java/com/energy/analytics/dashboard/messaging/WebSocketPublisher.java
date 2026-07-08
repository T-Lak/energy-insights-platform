package com.energy.analytics.dashboard.messaging;

import com.energy.analytics.crossborder.dto.FlowGridEdgeDTO;
import com.energy.analytics.crossborder.dto.FlowTotalsDTO;
import com.energy.analytics.dashboard.dto.SourceRankingPointDTO;
import com.energy.analytics.dashboard.dto.TimeseriesPointDTO;
import com.energy.analytics.crossborder.dto.RegionalFlowSnapshot;
import com.energy.analytics.crossborder.dto.RegionalFlowTotalsSnapshot;
import com.energy.analytics.dashboard.dto.DashboardMetricsTimeline;
import com.energy.analytics.dashboard.dto.RegionalRankingSnapshot;
import com.energy.analytics.crossborder.model.FlowPoint;
import com.energy.analytics.crossborder.model.projection.SourceContribution;
import com.energy.analytics.dashboard.model.DerivedMetric;
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
public class WebSocketPublisher {

   private final SimpMessagingTemplate messagingTemplate;

   public void sendKPIs(List<DerivedMetric> metrics, String region) {
      log.info("WebSocket Outbound: Received {} KPI metrics to broadcast for region {}", metrics.size(), region);

      Map<String, List<TimeseriesPointDTO>> metricMap = new HashMap<>();

      for (DerivedMetric metric : metrics) {
         List<TimeseriesPointDTO> dataPoints = metricMap.computeIfAbsent(metric.getMetric(), k -> new ArrayList<>());
         dataPoints.add(new TimeseriesPointDTO(metric.getTimestamp(), metric.getValue()));
      }

      messagingTemplate.convertAndSend(
              "/topic/grid_metrics",
              new DashboardMetricsTimeline(
                      region,
                      Instant.now(),
                      metricMap
              )
      );
   }

   public void sendSourceContributions(Map<String, List<SourceContribution>> data, String region) {
      log.info("WebSocket Outbound: Source contributions broadcast for region {}", region);

      Map<String, List<SourceRankingPointDTO>> dto = data.entrySet().stream()
              .collect(Collectors.toMap(
                      Map.Entry::getKey,
                      e -> e.getValue().stream()
                              .map(c -> new SourceRankingPointDTO(c.source(), c.value()))
                              .toList()
              ));

      messagingTemplate.convertAndSend(
              "/topic/grid_top_sources",
              new RegionalRankingSnapshot(
                      region,
                      Instant.now(),
                      dto
              )
      );
   }

   public void sendFlowPoints(List<FlowPoint> data, String region) {
      log.info("WebSocket Outbound: flow points broadcast for region {}", region);

      List<FlowGridEdgeDTO> dto = data.stream()
              .map(entity -> new FlowGridEdgeDTO(
                      entity.getTimestamp(),
                      entity.getFromRegion(),
                      entity.getToRegion(),
                      entity.getExportMW(),
                      entity.getImportMW()
              ))
              .toList();

      messagingTemplate.convertAndSend(
              "/topic/flow_points",
              new RegionalFlowSnapshot(
                      region,
                      Instant.now(),
                      dto
              )
      );
   }

   public void sendFlowTotals(FlowTotalsDTO dto, String region) {
      log.info("WebSocket Outbound: flow total broadcast for region {}", region);

      messagingTemplate.convertAndSend(
              "/topic/flow_totals",
              new RegionalFlowTotalsSnapshot(
                      region,
                      Instant.now(),
                      dto
              )
      );
   }

}
