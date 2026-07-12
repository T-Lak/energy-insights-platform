package com.energy.analytics.dashboard.dto;

import java.time.Instant;

public record DashboardKpiPointDTO(
     Instant timestamp,
     Double value,
     Double percentageChange
) {}
