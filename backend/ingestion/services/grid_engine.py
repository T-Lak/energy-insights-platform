from .data_utils import normalize_grid_data, prepare_kafka_payload
from messaging import send_event, producer
from providers import fetch_api, calculate_start_end_window


def sync_grid_data(client, logger):
    start, end = calculate_start_end_window()

    data_map = fetch_api(
        client=client,
        start=start,
        end=end,
        logger=logger
    )

    for category, df in data_map.items():
        df.index = df.index.tz_convert('Europe/Berlin')

        for timestamp, data in df.iterrows():
            data = normalize_grid_data(data.to_dict())
            event = prepare_kafka_payload(data, category, timestamp)

            key = f"DE|{category}|{timestamp.isoformat()}"

            send_event(
                topic='energy.raw',
                key=key,
                event=event,
                headers=[("__TypeId__", b"grid")],
                logger=logger,
            )

    producer.flush()
