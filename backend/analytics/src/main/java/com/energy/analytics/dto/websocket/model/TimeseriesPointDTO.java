package com.energy.analytics.dto.websocket.model;

import java.time.Instant;

public record TimeseriesPointDTO(
     Instant timestamp,
     Double value
) {}
