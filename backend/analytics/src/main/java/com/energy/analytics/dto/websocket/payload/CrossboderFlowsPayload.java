package com.energy.analytics.dto.websocket.payload;

import com.energy.analytics.dto.websocket.model.FlowPointDTO;

import java.time.Instant;
import java.util.List;

public record CrossboderFlowsPayload(
        String region,
        Instant timestamp,
        List<FlowPointDTO> flowPoints
) {}
