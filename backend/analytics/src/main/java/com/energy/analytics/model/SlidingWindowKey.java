package com.energy.analytics.model;

public record SlidingWindowKey(
        String region,
        String metric,
        String source,
        String category
) {}
