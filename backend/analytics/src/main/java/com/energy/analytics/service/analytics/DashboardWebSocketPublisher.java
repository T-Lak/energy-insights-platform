package com.energy.analytics.service.analytics;

import com.energy.analytics.dto.websocket.TimeseriesPointDTO;
import com.energy.analytics.dto.websocket.KpiTimeseriesDTO;
import com.energy.analytics.dto.websocket.SourceRankdingPointDTO;
import com.energy.analytics.dto.websocket.SourceRankingDTO;
import com.energy.analytics.model.projection.SourceContribution;
import com.energy.analytics.model.entity.DerivedMetric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class DashboardWebSocketPublisher {

   private final SimpMessagingTemplate messagingTemplate;

   public void sendKPIs(List<DerivedMetric> metrics, String region) {
      log.info("WebSocket Outbound: Received {} KPI metrics to broadcast", metrics.size());

      Map<String, List<TimeseriesPointDTO>> metricMap = new HashMap<>();

      for (DerivedMetric metric : metrics) {
         List<TimeseriesPointDTO> dataPoints = metricMap.computeIfAbsent(metric.getMetric(), k -> new ArrayList<>());
         dataPoints.add(new TimeseriesPointDTO(metric.getTimestamp(), metric.getValue()));
      }

      messagingTemplate.convertAndSend(
              "/topic/grid_metrics",
              new KpiTimeseriesDTO(
                      region,
                      Instant.now(),
                      metricMap
              )
      );
   }

   public void sendSourceContributions(Map<String, List<SourceContribution>> data, String region) {
      log.info("WebSocket Outbound: Source contributions broadcast");

      Map<String, List<SourceRankdingPointDTO>> dto = data.entrySet().stream()
              .collect(Collectors.toMap(
                      Map.Entry::getKey,
                      e -> e.getValue().stream()
                              .map(c -> new SourceRankdingPointDTO(c.source(), c.value()))
                              .toList()
              ));

      messagingTemplate.convertAndSend(
              "/topic/grid_sources",
              new SourceRankingDTO(
                      region,
                      Instant.now(),
                      dto
              )
      );
   }

}
