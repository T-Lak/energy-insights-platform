package com.energy.analytics.crossborder.dto;

import java.time.Instant;

public record FlowTotalsDTO(
     Instant timestamp,
     String region,
     float totalExportMW,
     float totalImportMW,
     double netExchangeMW
) {}
