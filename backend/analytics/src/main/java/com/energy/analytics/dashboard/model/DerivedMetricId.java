package com.energy.analytics.dashboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DerivedMetricId implements Serializable {
   private Instant timestamp;
   private String region;
   private String metric;
}
