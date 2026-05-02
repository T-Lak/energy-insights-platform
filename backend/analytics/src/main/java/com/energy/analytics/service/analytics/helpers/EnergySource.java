package com.energy.analytics.service.analytics.helpers;

import lombok.Getter;

@Getter
public enum EnergySource {

   BIOMASS(true),
   FOSSIL_BROWN_COAL(false),
   FOSSIL_COAL_DERIVED_GAS(false),
   FOSSIL_GAS(false),
   FOSSIL_HARD_COAL(false),
   FOSSIL_OIL(false),
   GEOTHERMAL(true),
   HYDRO_PUMPED_STORAGE(true),
   HYDRO_RUN_OF_RIVER(true),
   HYDRO_WATER_RESERVOIR(true),
   OTHER(false),
   OTHER_RENEWABLE(true),
   SOLAR(true),
   WASTE(true),
   WIND_OFFSHORE(true),
   WIND_ONSHORE(true);

   private final boolean renewable;

   EnergySource(boolean renewable) {
      this.renewable = renewable;
   }

}
