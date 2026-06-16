package com.energy.analytics.dto.websocket.model;

import java.time.Instant;

public record FlowTotalsDTO(
     Instant timestamp,
     String region,
     float totalExportMW,
     float totalImportMW,
     double netExchangeMW
) {}
