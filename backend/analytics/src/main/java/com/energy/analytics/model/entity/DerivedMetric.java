package com.energy.analytics.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "derived_metrics")
@IdClass(DerivedMetricId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DerivedMetric {
   @Id
   private Instant timestamp;
   @Id
   private String region;
   @Id
   private String metric;

   private Double value;
}
