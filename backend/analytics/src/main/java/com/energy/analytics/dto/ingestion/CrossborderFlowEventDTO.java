package com.energy.analytics.dto.ingestion;

import java.util.List;

public record CrossborderFlowEventDTO(
     String region,
     String metric,
     long timestamp,
     List<CrossborderFlowDataDTO> data
) {}
