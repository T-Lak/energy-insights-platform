from typing import List, Dict, Any

import pandas as pd


def normalize_grid_data(raw_data: Dict) -> List:
    normalized_data = []

    for key, val in raw_data.items():
        metric = {}
        if isinstance(key, tuple):
            metric['source'] = key[0].lower()
            metric['category'] = key[1].lower()
            metric['value'] = val
        else:
            metric['source'] = key.lower()
            metric['value'] = val
        normalized_data.append(metric)

    return normalized_data


def normalize_flow_data(timestamp, neighbor: str, export_value: Any, import_df: pd.Series) -> Dict:
    import_value = import_df.get(timestamp, 0.0)

    return {
        "timestamp": int(timestamp.timestamp()),
        "fromRegion": "DE_LU",
        "toRegion": neighbor,
        "exportMW": export_value,
        "importMW": float(import_value),
    }


def prepare_kafka_payload(data, category: str, timestamp) -> dict:
    return {
        "region": "DE_LU",
        "metric": category,
        "timestamp": int(timestamp.timestamp()),
        "data": data,
    }