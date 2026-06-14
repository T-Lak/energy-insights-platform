package com.energy.analytics.model.domain;

import lombok.Getter;

@Getter
public enum EmissionCategory {
   WIND_ONSHORE(11),
   WIND_OFFSHORE(12),
   SOLAR(48),
   HYDRO(24),
   NUCLEAR(12),
   BIOMASS(485),
   GAS(490),
   COAL(820),
   UNKNOWN(0);

   private final int emissionFactor;

   EmissionCategory(int emissionFactor) {
      this.emissionFactor = emissionFactor;
   }

}
