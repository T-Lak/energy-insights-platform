package com.energy.analytics.dto.websocket.payload;

import com.energy.analytics.dto.websocket.model.SourceRankingPointDTO;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record LiveSourceRankingPayload(
        String region,
        Instant timestamp,
        Map<String, List<SourceRankingPointDTO>> contributions
) {}