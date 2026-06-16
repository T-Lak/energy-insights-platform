package com.energy.analytics.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Table(name = "view_latest_kpi_snapshot")
@Immutable
@Data
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
