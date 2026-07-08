package com.energy.analytics.shared.mapper;

import com.energy.analytics.dashboard.dto.EnergyCategoryBreakdownDTO;
import com.energy.analytics.renewables.dto.RenewableMixPointDTO;
import com.energy.analytics.dashboard.model.DailyEnergySummary;
import com.energy.analytics.renewables.model.RenewableMix;

public class RenewablesMapper {

   public static RenewableMixPointDTO toRenewableMixDTO(RenewableMix renewableMix) {
      return new RenewableMixPointDTO(
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

   public static EnergyCategoryBreakdownDTO toDailyEnergySummaryDTO(DailyEnergySummary summary) {
      return new EnergyCategoryBreakdownDTO(
              summary.getTimePeriod(),
              summary.getCategory(),
              summary.getAmount(),
              summary.getMetricPoints()
      );
   }

}
