package com.energy.analytics.dto.rest;

import java.time.Instant;

public record CountryFlowSummaryDTO(
   Instant timestamp,
   String country,
   float totalImportMW,
   float totalExportMW,
   float netFlow,
   double importShortTermChangePercentage,
   double exportShortTermChangePercentage,
   double importLongTermChangePercentage,
   double exportLongTermChangePercentage
) {}
