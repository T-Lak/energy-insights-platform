package com.energy.analytics.dto.websocket;

import java.time.Instant;

public record TimeseriesPointDTO(
     Instant timestamp,
     Double value
) {}
