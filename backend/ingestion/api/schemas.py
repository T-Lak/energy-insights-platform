from pydantic import BaseModel
from datetime import date

class BackfillRequest(BaseModel):
    start_date: date
    end_date: date
    region: str