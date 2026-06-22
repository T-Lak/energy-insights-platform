package com.energy.analytics.dto.rest;

import com.energy.analytics.dto.websocket.model.FlowTotalsDTO;

import java.time.Instant;
import java.util.List;

public record CrossborderFlowTotalsTsPayload(
   String region,
   Instant generatedAt,
   List<FlowTotalsDTO> flowTotals
) {}
