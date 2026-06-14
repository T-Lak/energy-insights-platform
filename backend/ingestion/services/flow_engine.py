from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import Dict

import pandas as pd
from entsoe.exceptions import NoMatchingDataError

from providers import calculate_start_end_window
from messaging import send_event, producer
from providers import DE_NEIGHBORS
from services.data_utils import normalize_flow_data, prepare_kafka_payload


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


def sync_crossborder_flows(client, logger):
    start, end = calculate_start_end_window()

    with ThreadPoolExecutor(max_workers=4) as executor:
        futures = [
            executor.submit(fetch_flow_pair, client, neighbor, start, end)
            for neighbor in DE_NEIGHBORS
        ]

        flow_map = {}

        for future in as_completed(futures):
            try:
                neighbor, export_df, import_df = future.result()

                if export_df.empty and import_df.empty:
                    logger.warning(f"No flow data for {neighbor}")
                    continue

                for timestamp, export_value in export_df.items():
                    normalized_data: Dict = normalize_flow_data(timestamp, neighbor, export_value, import_df)
                    flow_map.setdefault(timestamp, []).append(normalized_data)

            except Exception as e:
                logger.error(f"Flow fetch failed: {e}")

        for timestamp, data in flow_map.items():
            event = prepare_kafka_payload(
                data,
                category="crossborder_flows",
                timestamp=timestamp,
            )

            send_event(
                topic="energy.flows",
                key=f"DE_LU|{neighbor}|{timestamp.isoformat()}",
                event=event,
                headers=[("__TypeId__", b"flows")],
                logger=logger,
            )

    producer.flush()