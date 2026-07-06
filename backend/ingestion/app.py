import os
import logging
from contextlib import asynccontextmanager

from dotenv import load_dotenv

from messaging.utils import EventType

load_dotenv()

from fastapi import FastAPI
from functools import partial
from apscheduler.schedulers.background import BackgroundScheduler
from entsoe import EntsoePandasClient

from api.endpoints import router as api_router

from services import sync_grid_data, sync_crossborder_flows



ENTSOE_API_KEY = os.getenv("ENTSOE_API_KEY")


@asynccontextmanager
async def lifespan(app: FastAPI):
    client = EntsoePandasClient(api_key=ENTSOE_API_KEY)
    logger = logging.getLogger("ingestion-service")

    app.state.entsoe_client = client
    app.state.logger = logger

    scheduler = BackgroundScheduler()

    grid_data_task = partial(sync_grid_data, client, logger, EventType.LIVE_METRICS)
    flow_data_task = partial(sync_crossborder_flows, client, logger, EventType.LIVE_FLOWS)

    scheduler.add_job(
        grid_data_task,
        id="grid-data-sync",
        trigger="cron",
        minute="0,15,30,45",
        misfire_grace_time=60,
        max_instances=1,
    )

    scheduler.add_job(
        flow_data_task,
        id="flow-data-sync",
        trigger="cron",
        minute="2,17,32,47",
        misfire_grace_time=60,
        max_instances=1,
    )

    logger.info("Starting ingestion scheduler...")

    scheduler.start()

    yield

    logger.info("Shutting down ingestion scheduler...")
    scheduler.shutdown()


app = FastAPI(title="GridPulse Ingestion Engine", lifespan=lifespan)

app.include_router(api_router)