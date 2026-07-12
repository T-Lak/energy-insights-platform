package com.energy.analytics.dashboard.dto;

public record DashboardSummarySnapshot(
     DashboardKpiPointDTO renewableShare,
     DashboardKpiPointDTO carbonIntensity,
     DashboardKpiPointDTO totalLoad,
     DashboardKpiPointDTO netBalance
) {}
