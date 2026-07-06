import pandas as pd


VALID_ENDPOINT_REGIONS = [
    "DE_LU",
]


DE_NEIGHBORS = [
    "FR",
    "NL",
    "BE",
    "CH",
    "AT",
    "CZ",
    "PL",
    "DK_1",
    "DK_2",
    "SE_4",
    "NO_2",
    "IT_NORD",
]


def calculate_2h_sliding_window():
    now = pd.Timestamp.now(tz="Europe/Berlin") - pd.Timedelta(hours=2)
    end = now.floor("15min")
    start = end - pd.Timedelta(hours=2)

    return start, end


def validate_dates(start_date: pd.Timestamp, end_date: pd.Timestamp):
    now = pd.Timestamp.now(tz="Europe/Berlin")
    end_of_day = now.ceil('D') - pd.Timedelta(microseconds=1)

    if start_date > end_date:
        raise ValueError("Start date must be less than end date.")
    if end_date > end_of_day:
        raise ValueError("End date must be less than now.")


def validate_region(region: str):
    if region not in VALID_ENDPOINT_REGIONS:
        raise ValueError("Region must be one of {}".format(VALID_ENDPOINT_REGIONS))