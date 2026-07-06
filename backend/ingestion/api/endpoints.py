from datetime import date
import pandas as pd

from fastapi import APIRouter, BackgroundTasks, Depends, Request, HTTPException
from starlette.status import HTTP_400_BAD_REQUEST

from api.dependencies import verify_api_key
from api.schemas import BackfillRequest
from messaging.utils import EventType

from providers import validate_dates, validate_region
from services import sync_grid_data, sync_crossborder_flows

router = APIRouter(prefix="/api/ingestion/backfill", tags=["metrics-backfill"])


def run_historical_metrics_backfill(client, logger, start_date: date, end_date: date, region: str):
    logger.info(f"Starting historical energy metrics sync for {region} from {start_date} to {end_date}")
    sync_grid_data(client, logger, EventType.BACKFILL_METRICS, start_date, end_date)


def run_historical_flows_backfill(client, logger, start_date: date, end_date: date, region: str):
    logger.info(f"Starting historical crossborder flows sync for {region} from {start_date} to {end_date}")
    sync_crossborder_flows(client, logger, EventType.BACKFILL_FLOWS, start_date, end_date)


@router.post("/metrics", dependencies=[Depends(verify_api_key)])
async def trigger_metrics_backfill(payload: BackfillRequest, background_tasks: BackgroundTasks, request: Request):
    client = request.app.state.entsoe_client
    logger = request.app.state.logger

    start = pd.Timestamp(f"{payload.start_date} 00:00:00").tz_localize("Europe/Berlin")
    end = pd.Timestamp(f"{payload.end_date} 23:59:59").tz_localize("Europe/Berlin")

    try:
        validate_dates(start, end)
        validate_region(payload.region)
    except ValueError as e:
        raise HTTPException(status_code=HTTP_400_BAD_REQUEST, detail=str(e))

    background_tasks.add_task(
        run_historical_metrics_backfill,
        client, logger, start, end, payload.region
    )

    return {"status": "queued", "message": "Metrics backfill job successfully queued."}


@router.post("/flows", dependencies=[Depends(verify_api_key)])
async def trigger_flows_backfill(payload: BackfillRequest, background_tasks: BackgroundTasks, request: Request):
    client = request.app.state.entsoe_client
    logger = request.app.state.logger

    start = pd.Timestamp(f"{payload.start_date} 00:00:00").tz_localize("Europe/Berlin")
    end = pd.Timestamp(f"{payload.end_date} 23:59:59").tz_localize("Europe/Berlin")

    try:
        validate_dates(start, end)
        validate_region(payload.region)
    except ValueError as e:
        raise HTTPException(status_code=HTTP_400_BAD_REQUEST, detail=str(e))

    background_tasks.add_task(
        run_historical_flows_backfill,
        client, logger, start, end, payload.region
    )

    return {"status": "queued", "message": "Crossborder flows backfill job successfully queued."}