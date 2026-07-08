package com.energy.analytics.crossborder.dto;

import java.time.Instant;

public record RegionalFlowTotalsSnapshot(
     String region,
     Instant timestamp,
     FlowTotalsDTO flowTotals
) {}
