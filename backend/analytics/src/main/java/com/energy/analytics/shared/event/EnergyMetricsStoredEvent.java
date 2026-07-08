package com.energy.analytics.shared.event;

import java.time.Instant;

public record EnergyMetricsStoredEvent(Instant timestamp, String region) {}
