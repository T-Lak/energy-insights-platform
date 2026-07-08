package com.energy.analytics.dashboard.dto;

import java.time.Instant;

public record TimeseriesPointDTO(
     Instant timestamp,
     Double value
) {}
