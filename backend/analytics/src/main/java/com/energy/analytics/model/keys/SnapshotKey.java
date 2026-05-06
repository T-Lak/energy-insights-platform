package com.energy.analytics.model.keys;

public record SnapshotKey(
     String metric,
     String source,
     String category
) {}
