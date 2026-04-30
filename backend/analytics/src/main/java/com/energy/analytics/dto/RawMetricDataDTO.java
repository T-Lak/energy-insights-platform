package com.energy.analytics.dto;

public record RawMetricDataDTO(
    String source,
    String category,
    Double value
) {}
