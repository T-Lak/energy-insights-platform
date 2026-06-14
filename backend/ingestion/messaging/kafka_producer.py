import json

from confluent_kafka import Producer


producer = Producer({
    'bootstrap.servers': 'localhost:9092',
})


def delivery_report(err, msg, logger):
    if err is not None:
        logger.error(f"Failed to deliver message: {err}")
    else:
        logger.info(f"Message delivered to {msg.topic()} [{msg.partition()}]")


def send_event(topic, key, event, headers, logger):
    producer.produce(
        topic=topic,
        key=key,
        value=json.dumps(event),
        headers=headers,
        callback=lambda err, msg: delivery_report(err, msg, logger),
    )
    producer.poll(0)