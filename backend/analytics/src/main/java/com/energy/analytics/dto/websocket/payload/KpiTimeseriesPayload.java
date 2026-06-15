package com.energy.analytics.dto.websocket.payload;

import com.energy.analytics.dto.websocket.model.TimeseriesPointDTO;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record KpiTimeseriesPayload(
  String region,
  Instant generatedAt,
  Map<String, List<TimeseriesPointDTO>> metrics
) {}
