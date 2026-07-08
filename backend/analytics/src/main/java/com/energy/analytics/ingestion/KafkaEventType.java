package com.energy.analytics.ingestion;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum KafkaEventType {

   LIVE_METRICS("live_metrics"),
   BACKFILL_METRICS("backfill_metrics"),
   LIVE_FLOWS("live_flows"),
   BACKFILL_FLOWS("backfill_flows");

   @JsonValue
   @Getter
   private final String value;
}
