package com.energy.analytics.dto.ingestion;

import com.energy.analytics.messaging.KafkaEventType;

import java.util.List;

public record CrossborderFlowEventDTO(
     KafkaEventType type,
     String region,
     String metric,
     List<CrossborderFlowDataDTO> data
) {}
