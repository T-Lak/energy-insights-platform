package com.energy.analytics.dto.rest;

import com.energy.analytics.dto.websocket.model.FlowPointDTO;

import java.util.List;

public record LatestFlowsPayload(
     List<FlowPointDTO> flowPoints
) {}
