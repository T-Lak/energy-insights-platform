package com.energy.analytics.renewables.model;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RenewableMix {
   private Instant timestamp;
   private Double solar;
   private Double windOnshore;
   private Double windOffshore;
   private Double biomass;
   private Double hydro;
   private Double geothermal;
   private Double otherRenewable;
}
