package com.energy.analytics.dto.ingestion;

import java.util.List;

public record RawEnergyEventDTO(
    String region,
    String metric,
    long timestamp,
    List<RawMetricDataDTO> data
) {}
