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
        ),
    }


def get_flow_query(client, from_region, to_region, start, end):
    return client.query_crossborder_flows(
        country_code_from=from_region,
        country_code_to=to_region,
        start=start,
        end=end
     )