from services import normalize_grid_data


def test_clean_data_flattens_tuple_keys():
    raw_payload = {
        ('Solar', 'Actual Aggregated'): 40000.5,
        ('Fossil Gas', 'Actual Consumption'): 357,
    }

    result = normalize_grid_data(raw_payload)

    assert result[0]['source'] == 'solar'
    assert result[0]['category'] == 'actual aggregated'
    assert result[0]['value'] == 40000.5

    assert result[1]['source'] == 'fossil gas'
    assert result[1]['category'] == 'actual consumption'
    assert result[1]['value'] == 357

    assert len(result) == 2


def test_clean_data_handles_simple_keys():
    simple_data = {"load": 50000}

    result = normalize_grid_data(simple_data)

    assert result[0]['source'] == 'load'
    assert result[0]['value'] == 50000


def test_clean_data_handles_empty_dict():
    raw_data = {}

    result = normalize_grid_data(raw_data)
    
    assert result == []