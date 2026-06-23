package com.energy.analytics.model.domain;

import lombok.Getter;

@Getter
public enum ContributionType {

   TOP_EMERGY_SOURCES("top_energy_sources"),
   TOP_CARBON_CONTRIBUTORS("top_carbon_contributors");

   private final String type;

   ContributionType(String type) {
      this.type = type;
   }

}
