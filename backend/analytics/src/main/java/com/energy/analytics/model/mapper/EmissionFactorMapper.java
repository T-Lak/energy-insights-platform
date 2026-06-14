package com.energy.analytics.model.mapper;

import com.energy.analytics.model.domain.EmissionCategory;
import com.energy.analytics.model.domain.EnergySource;

import java.util.HashMap;
import java.util.Map;

public class EmissionFactorMapper {

   private static final Map<EnergySource, EmissionCategory> MAP = new HashMap<>();

   static {
      MAP.put(EnergySource.WIND_ONSHORE, EmissionCategory.WIND_ONSHORE);
      MAP.put(EnergySource.WIND_OFFSHORE, EmissionCategory.WIND_OFFSHORE);
      MAP.put(EnergySource.SOLAR, EmissionCategory.SOLAR);

      MAP.put(EnergySource.FOSSIL_GAS, EmissionCategory.GAS);
      MAP.put(EnergySource.FOSSIL_COAL_DERIVED_GAS, EmissionCategory.GAS);

      MAP.put(EnergySource.BIOMASS, EmissionCategory.BIOMASS);

      MAP.put(EnergySource.FOSSIL_BROWN_COAL, EmissionCategory.COAL);
      MAP.put(EnergySource.FOSSIL_HARD_COAL, EmissionCategory.COAL);

      MAP.put(EnergySource.HYDRO_WATER_RESERVOIR, EmissionCategory.HYDRO);
      MAP.put(EnergySource.HYDRO_RUN_OF_RIVER, EmissionCategory.HYDRO);
      MAP.put(EnergySource.HYDRO_PUMPED_STORAGE, EmissionCategory.HYDRO);
   }

   public static EmissionCategory from(EnergySource source) {
      return MAP.getOrDefault(source, EmissionCategory.UNKNOWN);
   }
}
