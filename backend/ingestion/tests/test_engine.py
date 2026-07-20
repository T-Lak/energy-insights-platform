import pandas as pd

from messaging.utils import EventType
from services import sync_grid_data


def test_sync_latest_grid_data_calls_producer(mocker):
    mocker.patch('services.grid_engine.producer')

    mock_df = pd.DataFrame({
        'Actual Load': [40000.0]
    }, index=pd.to_datetime(['2026-04-26 14:00:00']).tz_localize('UTC'))

    mock_fetch = mocker.patch('services.grid_engine.fetch_api', return_value={'load': mock_df})

    mock_send = mocker.patch('services.grid_engine.send_event')

    mock_client = mocker.MagicMock()
    mock_logger = mocker.MagicMock()

    sync_grid_data(client=mock_client, logger=mock_logger, event_type=EventType.LIVE_METRICS)

    assert mock_fetch.called

    args, kwargs = mock_send.call_args
    assert kwargs['topic'] == 'energy.raw'
    assert kwargs['event']['metric'] == 'load'
    assert kwargs['event']['data'][0]['value'] == 40000.0
    assert "DE|load" in kwargs['key']