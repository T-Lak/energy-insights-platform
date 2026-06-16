package com.energy.analytics.dto.rest;

import com.energy.analytics.dto.websocket.model.TimeseriesPointDTO;

public record KpiSnapshotPayload(
   TimeseriesPointDTO renewableShare,
   TimeseriesPointDTO carbonIntensity,
   TimeseriesPointDTO totalLoad,
   TimeseriesPointDTO netBalance
) {}
