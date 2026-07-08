package com.energy.analytics.crossborder.dto;

import java.util.List;

public record CrossborderFlowsOverview(
     List<FlowGridEdgeDTO> hourlyFlowPoints,
     List<FlowTotalsDTO> flowTotals,
     List<CountryFlowMetricsDTO> countrySummaries
) {}
