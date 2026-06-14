package com.energy.analytics.dto.ingestion;

public record CrossborderFlowDataDTO(
     long timestamp,
     String fromRegion,
     String toRegion,
     float exportMW,
     float importMW
) {}
