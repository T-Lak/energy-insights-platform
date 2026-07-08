package com.energy.analytics.ingestion.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricId implements Serializable {
    private Instant timestamp;
    private String region;
    private String metric;
    private String source;
    private String category;
}
