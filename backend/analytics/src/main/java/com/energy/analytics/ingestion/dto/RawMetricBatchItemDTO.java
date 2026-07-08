package com.energy.analytics.ingestion.dto;


public record RawMetricBatchItemDTO(
    long timestamp,
    String source,
    String category,
    Double value
) {}
