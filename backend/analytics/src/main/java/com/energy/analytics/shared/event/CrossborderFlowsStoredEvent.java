package com.energy.analytics.shared.event;

import java.time.Instant;

public record CrossborderFlowsStoredEvent(Instant timestamp, String region) {}
