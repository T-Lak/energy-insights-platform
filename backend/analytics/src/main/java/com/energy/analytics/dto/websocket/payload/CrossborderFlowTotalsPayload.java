package com.energy.analytics.dto.websocket.payload;

import com.energy.analytics.dto.websocket.model.FlowTotalsDTO;

import java.time.Instant;

public record CrossborderFlowTotalsPayload(
     String region,
     Instant timestamp,
     FlowTotalsDTO flowTotals
) {}
