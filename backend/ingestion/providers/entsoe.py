from requests import HTTPError, Timeout
import traceback

from .queries import get_query_map


def fetch_api(client, start, end, logger):
    query_map = get_query_map(client, start, end)
    data_map = {}

    for category, fn in query_map.items():
        try:
            df = fn()

            if df.empty:
                logger.warning(f"No data returned for {category} between {start} and {end}.")
                continue

            data_map[category] = df
            logger.info(f"Fetched {len(df)} rows for {category}")
        except HTTPError as e:
            if e.response.status_code == 401:
                logger.error("Invalid ENTSO-E API key.")
            elif e.response.status_code == 429:
                logger.warning("Rate limit exceeded.")
            else:
                logger.error(f"ENTSO-E API returned error: {e}")
        except (ConnectionError, Timeout) as e:
            logger.error(f"Network level failure: {e}")
        except Exception as e:
            error_type = type(e).__name__
            stack_trace = traceback.format_exc()
            logger.error(f"Unexpected error [{error_type}]: {e}")
            logger.debug(f"Full stack trace:\n{stack_trace}")

    return data_map