package com.energy.analytics.service.analytics;

import com.energy.analytics.dto.websocket.model.FlowPointDTO;
import com.energy.analytics.dto.websocket.model.FlowTotalsDTO;
import com.energy.analytics.dto.websocket.model.SourceRankingPointDTO;
import com.energy.analytics.dto.websocket.model.TimeseriesPointDTO;
import com.energy.analytics.dto.websocket.payload.CrossboderFlowsPayload;
import com.energy.analytics.dto.websocket.payload.CrossborderFlowTotalsPayload;
import com.energy.analytics.dto.websocket.payload.KpiTimeseriesPayload;
import com.energy.analytics.dto.websocket.payload.LiveSourceRankingPayload;
import com.energy.analytics.model.entity.FlowPoint;
import com.energy.analytics.model.projection.SourceContribution;
import com.energy.analytics.model.entity.DerivedMetric;
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
              new KpiTimeseriesPayload(
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
              new LiveSourceRankingPayload(
                      region,
                      Instant.now(),
                      dto
              )
      );
   }

   public void sendFlowPoints(List<FlowPoint> data, String region) {
      log.info("WebSocket Outbound: flow points broadcast for region {}", region);

      List<FlowPointDTO> dto = data.stream()
              .map(entity -> new FlowPointDTO(
                      entity.getTimestamp(),
                      entity.getFromRegion(),
                      entity.getToRegion(),
                      entity.getExportMW(),
                      entity.getImportMW()
              ))
              .toList();

      messagingTemplate.convertAndSend(
              "/topic/flow_points",
              new CrossboderFlowsPayload(
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
              new CrossborderFlowTotalsPayload(
                      region,
                      Instant.now(),
                      dto
              )
      );
   }

}
