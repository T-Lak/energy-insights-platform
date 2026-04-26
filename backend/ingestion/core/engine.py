import pandas as pd

from .data_utils import clean_data
from messaging import send_event, producer
from providers import fetch_api


def execute_15min_process(client, logger):
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
            event = {
                'region': "DE_LU",
                'metric': category,
                'timestamp': timestamp.isoformat(),
                'data': clean_data(data.to_dict()),
            }

            key = f"DE|{category}|{timestamp.isoformat()}"

            print('event: ', event)

            send_event(
                topic='energy.raw',
                key=key,
                event=event,
                logger=logger,
            )

    producer.flush()