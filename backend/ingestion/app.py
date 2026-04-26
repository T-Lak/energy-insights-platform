import logging
import time

from functools import partial
from apscheduler.schedulers.background import BackgroundScheduler
from entsoe import EntsoePandasClient

from config import ENTSOE_API_KEY
from core import execute_15min_process


def main():
    client = EntsoePandasClient(api_key=ENTSOE_API_KEY)
    logger = logging.getLogger("ingestion-service")
    scheduler = BackgroundScheduler()

    task = partial(
        execute_15min_process,
        client=client,
        logger=logger
    )

    scheduler.add_job(
        task,
        trigger="cron",
        minute="0,15,30,45",
        misfire_grace_time=60,
    )

    scheduler.start()

    try:
        while True:
            time.sleep(100)
    except (KeyboardInterrupt, SystemExit):
        scheduler.shutdown()


if __name__ == "__main__":
    main()