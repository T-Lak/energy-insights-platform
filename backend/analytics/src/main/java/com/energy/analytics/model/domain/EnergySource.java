package com.energy.analytics.model.domain;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum EnergySource {

   BIOMASS(true),
   FOSSIL_BROWN_COAL(false),
   FOSSIL_COAL_DERIVED_GAS(false),
   FOSSIL_GAS(false),
   FOSSIL_HARD_COAL(false),
   FOSSIL_OIL(false),
   GEOTHERMAL(true),
   HYDRO_PUMPED_STORAGE(false),
   HYDRO_RUN_OF_RIVER(true),
   HYDRO_WATER_RESERVOIR(true),
   OTHER(false),
   OTHER_RENEWABLE(true),
   SOLAR(true),
   WASTE(false),
   WIND_OFFSHORE(true),
   WIND_ONSHORE(true);

   private final boolean renewable;

   EnergySource(boolean renewable) {
      this.renewable = renewable;
   }

   public static Set<EnergySource> criticalGenerationSources() {
      return EnumSet.of(
              SOLAR,
              WIND_ONSHORE,
              WIND_OFFSHORE,
              FOSSIL_GAS,
              FOSSIL_BROWN_COAL,
              FOSSIL_OIL,
              FOSSIL_HARD_COAL
      );
   }

}
