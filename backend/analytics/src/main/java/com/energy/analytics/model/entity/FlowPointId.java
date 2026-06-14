package com.energy.analytics.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowPointId {
   private Instant timestamp;
   private String fromRegion;
   private String toRegion;
   private float exportMW;
   private float importMW;
}
