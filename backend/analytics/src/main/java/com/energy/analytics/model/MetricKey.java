package com.energy.analytics.model;

public record MetricKey(
        String metric,
        String source,
        String category
) {}
