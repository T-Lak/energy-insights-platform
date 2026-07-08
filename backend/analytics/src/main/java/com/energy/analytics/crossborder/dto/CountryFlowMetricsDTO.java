package com.energy.analytics.crossborder.dto;

import java.time.Instant;

public record CountryFlowMetricsDTO(
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
