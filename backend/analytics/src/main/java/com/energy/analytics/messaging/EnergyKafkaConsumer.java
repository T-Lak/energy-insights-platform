package com.energy.analytics.messaging;

import com.energy.analytics.dto.ingestion.CrossborderFlowEventDTO;
import com.energy.analytics.dto.ingestion.RawEnergyEventDTO;
import com.energy.analytics.service.ingestion.EnergyPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EnergyKafkaConsumer {

    private final EnergyPersistenceService persistenceService;

    @KafkaListener(topics = "energy.raw", groupId = "analytics-group-v1")
    public void consumeRawGridData(RawEnergyEventDTO payload) {
        log.info("Received {} metrics for region {}", payload.data().size(), payload.region());

        persistenceService.processRawEnergyMetrics(payload);
    }

    @KafkaListener(topics = "energy.flows", groupId = "analytics-group-v1")
    public void consumeCrossborderFlowData(CrossborderFlowEventDTO payload) {
        log.info("Received flows for region {}", payload.region());

        persistenceService.processCrossborderFlows(payload);
    }

}
