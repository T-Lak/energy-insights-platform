package com.energy.analytics.crossborder.dto;

import java.time.Instant;
import java.util.List;

public record RegionalFlowTotalsTimeline(
   String region,
   Instant generatedAt,
   List<FlowTotalsDTO> flowTotals
) {}
