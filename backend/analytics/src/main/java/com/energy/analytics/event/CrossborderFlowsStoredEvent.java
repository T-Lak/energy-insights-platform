package com.energy.analytics.event;

import java.time.Instant;

public record CrossborderFlowsStoredEvent(Instant timestamp, String region) {}
