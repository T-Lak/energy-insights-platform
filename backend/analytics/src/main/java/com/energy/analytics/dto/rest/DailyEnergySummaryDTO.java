package com.energy.analytics.dto.rest;

import java.util.List;

public record DailyEnergySummaryDTO(
  String timePeriod,
  String category,
  double amount,
  List<MetricPointDTO> metricPoints
) {}
