import pandas as pd


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


def calculate_start_end_window():
    now = pd.Timestamp.now(tz="Europe/Berlin") - pd.Timedelta(hours=2)
    end = now.floor("15min")
    start = end - pd.Timedelta(hours=2)

    return start, end