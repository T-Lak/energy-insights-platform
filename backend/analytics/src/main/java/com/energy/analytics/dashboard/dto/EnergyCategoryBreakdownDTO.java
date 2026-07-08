package com.energy.analytics.dashboard.dto;

import java.util.List;

public record EnergyCategoryBreakdownDTO(
  String timePeriod,
  String category,
  double amount,
  List<SourceContributionPointDTO> metricPoints
) {}
