package com.energy.analytics.dto.websocket.model;

import java.time.Instant;

public record FlowPointDTO(
     Instant timestamp,
     String fromRegion,
     String toRegion,
     float exportMW,
     float importMW
) {}
