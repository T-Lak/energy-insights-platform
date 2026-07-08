package com.energy.analytics.ingestion.dto;

public record CrossborderFlowDataDTO(
     long timestamp,
     String fromRegion,
     String toRegion,
     float exportMW,
     float importMW
) {}
