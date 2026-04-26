def get_query_map(client, start, end):
    return {
        'load': lambda: client.query_load(
            country_code='DE_LU',
            start=start,
            end=end
        ),
        'generation': lambda: client.query_generation(
            country_code='DE_LU',
            start=start,
            end=end
        )
    }