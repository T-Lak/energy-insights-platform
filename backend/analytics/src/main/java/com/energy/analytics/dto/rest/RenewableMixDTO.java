package com.energy.analytics.dto.rest;

import java.time.Instant;

public record RenewableMixDTO(
   Instant timestamp,
   Double solar,
   Double windOnshore,
   Double windOffshore,
   Double biomass,
   Double hydro,
   Double geothermal,
   Double otherRenewable
) {}
