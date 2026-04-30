from typing import List, Dict


def normalize_data(raw_data: Dict) -> List:
    normalized_data = []
    for key, val in raw_data.items():
        metric = {}
        if isinstance(key, tuple):
            metric['source'] = key[0].lower()
            metric['category'] = key[1].lower()
            metric['value'] = val
        else:
            metric['source'] = key
            metric['value'] = val
        normalized_data.append(metric)

    return normalized_data


def prepare_kafka_payload(data: List, category: str, timestamp) -> Dict:
    unix_ts = int(timestamp.timestamp())

    return {
        "region": "DE_LU",
        "metric": category,
        "timestamp": unix_ts,
        "data": data,
    }