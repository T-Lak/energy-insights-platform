import pandas as pd

from .data_utils import normalize_data, prepare_kafka_payload
from messaging import send_event, producer
from providers import fetch_api


def sync_grid_data(client, logger):
    now = pd.Timestamp.now(tz="Europe/Berlin") - pd.Timedelta(hours=2)
    end = now.floor("15min")
    start = end - pd.Timedelta(hours=2)

    data_map = fetch_api(
        client=client,
        start=start,
        end=end,
        logger=logger
    )

    for category, df in data_map.items():
        df.index = df.index.tz_convert('Europe/Berlin')
        for timestamp, data in df.iterrows():
            data = normalize_data(data.to_dict())
            event = prepare_kafka_payload(data, category, timestamp)

            key = f"DE|{category}|{timestamp.isoformat()}"

            send_event(
                topic='energy.raw',
                key=key,
                event=event,
                logger=logger,
            )

    producer.flush()