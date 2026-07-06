from .data_utils import normalize_grid_data
from messaging import send_event, producer
from messaging.utils import prepare_kafka_payload
from providers import fetch_api, calculate_2h_sliding_window


def sync_grid_data(client, logger, event_type, start_date=None, end_date=None, chunk_size=1000):
    if start_date is None or end_date is None:
        start_date, end_date = calculate_2h_sliding_window()

    data_map = fetch_api(
        client=client,
        start=start_date,
        end=end_date,
        logger=logger
    )

    for category, df in data_map.items():
        df.index = df.index.tz_convert('Europe/Berlin')

        batch = []

        for timestamp, data in df.iterrows():
            data = normalize_grid_data(data.to_dict(), timestamp)

            batch.extend(data)

        kafka_key = f"DE|{category}"

        for i in range(0, len(batch), chunk_size):
            chunk = batch[i:i + chunk_size]
            event = prepare_kafka_payload(chunk, category, event_type)

            send_event(
                topic='energy.raw',
                key=kafka_key,
                event=event,
                headers=[("__TypeId__", b"grid")],
                logger=logger,
            )

    producer.flush()
