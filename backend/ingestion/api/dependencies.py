import os

from fastapi import Security, HTTPException
from fastapi.security import APIKeyHeader
from starlette.status import HTTP_403_FORBIDDEN


API_KEY_NAME = "X-API-Key"
api_key_header = APIKeyHeader(name=API_KEY_NAME, auto_error=False)

INGESTION_API_KEY = os.getenv("INGESTION_API_KEY")


async def verify_api_key(api_key: str = Security(api_key_header)):
    if api_key != INGESTION_API_KEY:
        raise HTTPException(status_code=HTTP_403_FORBIDDEN, detail="Access Denied. Invalid or missing API key.")
    return api_key