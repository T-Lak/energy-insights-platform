from enum import Enum


class EventType(Enum):
    LIVE_METRICS = "live_metrics"
    BACKFILL_METRICS = "backfill_metrics"
    LIVE_FLOWS = "live_flows"
    BACKFILL_FLOWS = "backfill_flows"


def prepare_kafka_payload(data_batch, category: str, type: EventType) -> dict:
    return {
        "type": type.value,
        "region": "DE_LU",
        "metric": category,
        "data": data_batch,
    }