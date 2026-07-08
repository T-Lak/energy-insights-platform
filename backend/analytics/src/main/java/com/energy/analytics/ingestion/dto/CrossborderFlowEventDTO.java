package com.energy.analytics.ingestion.dto;

import com.energy.analytics.ingestion.KafkaEventType;

import java.util.List;

public record CrossborderFlowEventDTO(
     KafkaEventType type,
     String region,
     String metric,
     List<CrossborderFlowDataDTO> data
) {}
