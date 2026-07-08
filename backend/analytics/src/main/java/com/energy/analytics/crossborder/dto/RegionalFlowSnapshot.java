package com.energy.analytics.crossborder.dto;

import java.time.Instant;
import java.util.List;

public record RegionalFlowSnapshot(
        String region,
        Instant timestamp,
        List<FlowGridEdgeDTO> flowPoints
) {}
