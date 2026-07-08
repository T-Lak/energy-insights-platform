package com.energy.analytics.renewables.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DailyRenewablesSummary {
   private Instant timestamp;
   private String source;
   private String region;
   private Double avgGenerationMW;
}
