package com.energy.analytics.dashboard.dto;

public record DashboardSummarySnapshot(
   TimeseriesPointDTO renewableShare,
   TimeseriesPointDTO carbonIntensity,
   TimeseriesPointDTO totalLoad,
   TimeseriesPointDTO netBalance
) {}
