package com.energy.analytics.crossborder.dto;

import java.util.List;

public record LatestFlowsSnapshot(
     List<FlowGridEdgeDTO> flowPoints
) {}
