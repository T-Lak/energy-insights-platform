package com.energy.analytics.dashboard.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record RegionalRankingSnapshot(
        String region,
        Instant timestamp,
        Map<String, List<SourceRankingPointDTO>> contributions
) {}