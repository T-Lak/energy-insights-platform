package com.energy.analytics.dto.rest;

import com.energy.analytics.dto.websocket.model.FlowPointDTO;
import com.energy.analytics.dto.websocket.model.FlowTotalsDTO;

import java.util.List;

public record CrossborderFlowsPayload(
     List<FlowPointDTO> hourlyFlowPoints,
     List<FlowTotalsDTO> flowTotals,
     List<CountryFlowSummaryDTO> countrySummaries
) {}
