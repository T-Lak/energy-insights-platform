import os

from pathlib import Path

from fastapi import Security, HTTPException
from fastapi.security import APIKeyHeader
from starlette.status import HTTP_403_FORBIDDEN


API_KEY_NAME = "X-API-Key"
api_key_header = APIKeyHeader(name=API_KEY_NAME, auto_error=False)

def get_secret(name: str) -> str | None:
    secret_path = Path(f"/run/secrets/{name}")

    if secret_path.exists():
        return secret_path.read_text().strip()

    return os.getenv(name)


INGESTION_API_KEY = get_secret("INGESTION_API_KEY")


async def verify_api_key(api_key: str = Security(api_key_header)):
    if api_key != INGESTION_API_KEY:
        raise HTTPException(status_code=HTTP_403_FORBIDDEN, detail="Access Denied. Invalid or missing API key.")
    return api_key