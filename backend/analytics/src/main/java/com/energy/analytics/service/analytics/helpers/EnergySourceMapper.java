package com.energy.analytics.service.analytics.helpers;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class EnergySourceMapper {

   private static final Map<String, EnergySource> MAP = Map.ofEntries(
           Map.entry("biomass", EnergySource.BIOMASS),
           Map.entry("fossil brown coal/lignite", EnergySource.FOSSIL_BROWN_COAL),
           Map.entry("fossil coal-derived gas", EnergySource.FOSSIL_COAL_DERIVED_GAS),
           Map.entry("fossil gas", EnergySource.FOSSIL_GAS),
           Map.entry("fossil hard coal", EnergySource.FOSSIL_HARD_COAL),
           Map.entry("fossil oil", EnergySource.FOSSIL_OIL),
           Map.entry("geothermal", EnergySource.GEOTHERMAL),
           Map.entry("hydro pumped storage", EnergySource.HYDRO_PUMPED_STORAGE),
           Map.entry("hydro run-of-river and poundage", EnergySource.HYDRO_RUN_OF_RIVER),
           Map.entry("hydro water reservoir", EnergySource.HYDRO_WATER_RESERVOIR),
           Map.entry("other", EnergySource.OTHER),
           Map.entry("other renewable", EnergySource.OTHER_RENEWABLE),
           Map.entry("solar", EnergySource.SOLAR),
           Map.entry("waste", EnergySource.WASTE),
           Map.entry("wind offshore", EnergySource.WIND_OFFSHORE),
           Map.entry("wind onshore", EnergySource.WIND_ONSHORE)
   );

   public static EnergySource from(String raw) {
      return Optional.ofNullable(MAP.get(raw.toLowerCase()))
              .orElseThrow(() -> new IllegalArgumentException("Unknown source: " + raw));
   }
}
