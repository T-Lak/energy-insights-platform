package com.energy.analytics.dto.ingestion;

public record RawMetricDataDTO(
    String source,
    String category,
    Double value
) {}
