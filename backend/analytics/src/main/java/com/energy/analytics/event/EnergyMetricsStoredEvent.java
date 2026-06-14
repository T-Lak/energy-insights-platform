package com.energy.analytics.event;

import java.time.Instant;

public record EnergyMetricsStoredEvent(Instant timestamp, String region) {}
