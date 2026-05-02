package com.energy.analytics.messaging;

import com.energy.analytics.dto.RawEnergyEventDTO;
import com.energy.analytics.service.ingestion.EnergyMetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EnergyKafkaConsumer {

    private final EnergyMetricService metricService;

    @KafkaListener(topics = "energy.raw", groupId = "analytics-group-v1")
    public void consumeRaw(RawEnergyEventDTO payload) {
        log.info("Received {} metrics for region {}", payload.data().size(), payload.region());

        metricService.processMetrics(payload);
    }

}
