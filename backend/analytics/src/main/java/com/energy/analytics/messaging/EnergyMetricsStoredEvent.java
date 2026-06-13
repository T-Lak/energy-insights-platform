package com.energy.analytics.messaging;

import java.time.Instant;

public record EnergyMetricsStoredEvent(Instant timestamp, String region) {}
