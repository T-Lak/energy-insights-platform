package com.energy.analytics.dto.ingestion;


public record RawMetricBatchItemDTO(
    long timestamp,
    String source,
    String category,
    Double value
) {}
