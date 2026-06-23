package com.energy.analytics.model.domain;

import lombok.Getter;

@Getter
public enum EmissionCategory {
   // Zero emissions sources
   WIND_ONSHORE(0),
   WIND_OFFSHORE(0),
   SOLAR(0),
   HYDRO(0),
   NUCLEAR(0),

   // Combustion Sources (gCO2 / kWh of electrical output)
   BIOMASS(400),              // Standard European CCGT plant average
   GAS(400),                  // Pure physical stack output (combustion)
   FOSSIL_HARD_COAL(820),     // Hard coal generation average
   FOSSIL_BROWN_COAL(1000),   // Lignite / Brown coal

   UNKNOWN(0);

   private final int emissionFactor;

   EmissionCategory(int emissionFactor) {
      this.emissionFactor = emissionFactor;
   }

}
