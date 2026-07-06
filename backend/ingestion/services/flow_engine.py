from collections import defaultdict
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import Dict
import threading

import pandas as pd
from entsoe.exceptions import NoMatchingDataError

from messaging import send_event, producer
from messaging.utils import prepare_kafka_payload
from providers import DE_NEIGHBORS, calculate_2h_sliding_window
from services.data_utils import normalize_flow_data


def fetch_flow_pair(client, neighbor, start, end):
    try:
        export_df = client.query_crossborder_flows(
            country_code_from="DE_LU",
            country_code_to=neighbor,
            start=start,
            end=end
        )
    except NoMatchingDataError:
        export_df = pd.DataFrame()

    try:
        import_df = client.query_crossborder_flows(
            country_code_from=neighbor,
            country_code_to="DE_LU",
            start=start,
            end=end
        )
    except NoMatchingDataError:
        import_df = pd.DataFrame()

    return neighbor, export_df, import_df


def sync_crossborder_flows(client, logger, event_type, start_date=None, end_date=None, chunk_size=1000):
    if start_date is None or end_date is None:
        start_date, end_date = calculate_2h_sliding_window()

    flow_map = defaultdict(list)
    map_lock = threading.Lock()

    with ThreadPoolExecutor(max_workers=4) as executor:
        futures = [
            executor.submit(fetch_flow_pair, client, neighbor, start_date, end_date)
            for neighbor in DE_NEIGHBORS
        ]

        for future in as_completed(futures):
            try:
                neighbor, export_df, import_df = future.result()

                if export_df.empty and import_df.empty:
                    logger.warning(f"No flow data for {neighbor}")
                    continue

                with map_lock:
                    for timestamp, export_value in export_df.items():
                        normalized_data: Dict = normalize_flow_data(timestamp, neighbor, export_value, import_df)
                        flow_map[timestamp].append(normalized_data)

            except Exception as e:
                logger.error(f"Flow fetch failed: {e}")

        batch = []

        for timestamp, data_list in flow_map.items():
            batch.extend(data_list)

        kafka_key = "DE_LU|ALL_NEIGHBORS"

        for i in range(0, len(batch), chunk_size):
            chunk = batch[i:i + chunk_size]
            event = prepare_kafka_payload(chunk, "crossborder_flows", event_type)

            send_event(
                topic="energy.flows",
                key=kafka_key,
                event=event,
                headers=[("__TypeId__", b"flows")],
                logger=logger,
            )

    producer.flush()