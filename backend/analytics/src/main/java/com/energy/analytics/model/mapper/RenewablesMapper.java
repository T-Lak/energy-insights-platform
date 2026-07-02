package com.energy.analytics.model.mapper;

import com.energy.analytics.dto.rest.RenewableMixDTO;
import com.energy.analytics.model.entity.RenewableMix;

public class RenewablesMapper {

   public static RenewableMixDTO toDTO(RenewableMix renewableMix) {
      return new RenewableMixDTO(
              renewableMix.getTimestamp(),
              renewableMix.getSolar(),
              renewableMix.getWindOnshore(),
              renewableMix.getWindOffshore(),
              renewableMix.getBiomass(),
              renewableMix.getHydro(),
              renewableMix.getGeothermal(),
              renewableMix.getOtherRenewable()
      );
   }

}
