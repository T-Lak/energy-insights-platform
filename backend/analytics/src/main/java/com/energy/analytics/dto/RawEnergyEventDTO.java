package com.energy.analytics.dto;

import java.util.List;

public record RawEnergyEventDTO(
    String region,
    String metric,
    long timestamp,
    List<RawMetricDataDTO> data
) {}
