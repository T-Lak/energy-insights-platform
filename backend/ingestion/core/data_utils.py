def clean_data(raw_data):
    _clean_data = {}
    for key, val in raw_data.items():
        if isinstance(key, tuple):
            clean_key = " - ".join((str(part) for part in key))
            _clean_data[clean_key] = val
        else:
            _clean_data[key] = val

    return _clean_data