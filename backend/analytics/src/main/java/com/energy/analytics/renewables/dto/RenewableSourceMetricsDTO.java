package com.energy.analytics.renewables.dto;

import java.time.Instant;

public record RenewableSourceMetricsDTO(
     Instant timestamp,
     String source,
     String region,
     double avgGenerationMW,
     double change1hPercentage,
     double change24hPercentage
) {}
