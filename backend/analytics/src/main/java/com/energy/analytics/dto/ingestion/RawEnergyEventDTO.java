package com.energy.analytics.dto.ingestion;

import com.energy.analytics.messaging.KafkaEventType;

import java.util.List;

public record RawEnergyEventDTO(
    KafkaEventType type,
    String region,
    String metric,
    List<RawMetricBatchItemDTO> data
) {}
