package com.energy.analytics.dashboard.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Table(name = "view_latest_kpi_snapshot")
@Immutable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiSnapshotView {
   @Id
   @Column(name = "bucket")
   private Instant bucket;
   private String region;
   private Double renewableShare;
   private Double carbonIntensity;
   private Double totalLoad;
   private Double netBalance;
}
