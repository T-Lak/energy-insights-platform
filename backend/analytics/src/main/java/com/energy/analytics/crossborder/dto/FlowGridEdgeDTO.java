package com.energy.analytics.crossborder.dto;

import java.time.Instant;

public record FlowGridEdgeDTO(
     Instant timestamp,
     String fromRegion,
     String toRegion,
     float exportMW,
     float importMW
) {}
