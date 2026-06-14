import logging
import time

from functools import partial
from apscheduler.schedulers.background import BackgroundScheduler
from entsoe import EntsoePandasClient

from config import ENTSOE_API_KEY
from services import sync_grid_data, sync_crossborder_flows


def main():
    client = EntsoePandasClient(api_key=ENTSOE_API_KEY)
    logger = logging.getLogger("ingestion-service")
    scheduler = BackgroundScheduler()

    grid_data_task = partial(
        sync_grid_data,
        client=client,
        logger=logger
    )

    flow_data_task = partial(
        sync_crossborder_flows,
        client=client,
        logger=logger
    )

    scheduler.add_job(
        grid_data_task,
        id="grid-data-sync",
        trigger="cron",
        minute="0,30,30,45",
        misfire_grace_time=60,
        max_instances=1,
    )

    scheduler.add_job(
        flow_data_task,
        id="flow-data-sync",
        trigger="cron",
        minute="2,32,32,47",
        misfire_grace_time=60,
        max_instances=1,
    )

    logger.info("Starting ingestion scheduler...")

    scheduler.start()

    try:
        while True:
            time.sleep(100)
    except (KeyboardInterrupt, SystemExit):
        scheduler.shutdown()


if __name__ == "__main__":
    main()