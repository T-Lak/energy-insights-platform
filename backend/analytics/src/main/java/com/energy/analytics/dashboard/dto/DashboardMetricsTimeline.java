package com.energy.analytics.dashboard.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record DashboardMetricsTimeline(
  String region,
  Instant generatedAt,
  Map<String, List<TimeseriesPointDTO>> metrics
) {}
