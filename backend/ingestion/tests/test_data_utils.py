from core import clean_data


def test_clean_data_flattens_tuple_keys():
    raw_payload = {
        ('Solar', 'Actual Aggregated'): 40000.5,
        'Regular Key': 123.45
    }

    result = clean_data(raw_payload)

    assert "Solar - Actual Aggregated" in result
    assert result["Solar - Actual Aggregated"] == 40000.5
    assert result["Regular Key"] == 123.45
    assert len(result) == 2


def test_clean_data_handles_simple_keys():
    simple_data = {"load": 50000}

    result = clean_data(simple_data)

    assert result["load"] == 50000


def test_clean_data_handles_empty_dict():
    raw_data = {}

    result = clean_data(raw_data)
    
    assert result == {}