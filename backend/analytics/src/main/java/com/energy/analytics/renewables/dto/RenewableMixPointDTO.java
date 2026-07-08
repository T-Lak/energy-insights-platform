package com.energy.analytics.renewables.dto;

import java.time.Instant;

public record RenewableMixPointDTO(
   Instant timestamp,
   Double solar,
   Double windOnshore,
   Double windOffshore,
   Double biomass,
   Double hydro,
   Double geothermal,
   Double otherRenewable
) {}
