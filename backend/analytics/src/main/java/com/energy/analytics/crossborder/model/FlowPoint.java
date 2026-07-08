package com.energy.analytics.crossborder.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "crossborder_flows")
@IdClass(FlowPointId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowPoint {
   @Id
   private Instant timestamp;
   @Id
   private String fromRegion;
   @Id
   private String toRegion;
   @Column(name = "export_mw")
   private float exportMW;
   @Column(name = "import_mw")
   private float importMW;
}
