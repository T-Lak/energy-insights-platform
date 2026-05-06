package com.energy.analytics.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawMetricId implements Serializable {
    private Instant timestamp;
    private String region;
    private String metric;
    private String source;
    private String category;
}
