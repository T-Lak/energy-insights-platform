package com.energy.analytics.dto.websocket;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record SourceRankingDTO(
        String region,
        Instant timestamp,
        Map<String, List<SourceRankdingPointDTO>> contributions
) {}