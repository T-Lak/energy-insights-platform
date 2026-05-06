package com.energy.analytics.model.keys;

public record SlidingWindowKey(
        String region,
        String metric,
        String source,
        String category
) {}
