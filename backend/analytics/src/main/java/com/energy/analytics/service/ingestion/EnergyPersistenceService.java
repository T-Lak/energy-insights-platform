package com.energy.analytics.service.ingestion;

import com.energy.analytics.dto.ingestion.CrossborderFlowEventDTO;
import com.energy.analytics.dto.ingestion.RawEnergyEventDTO;
import com.energy.analytics.event.CrossborderFlowsStoredEvent;
import com.energy.analytics.event.EnergyMetricsStoredEvent;
import com.energy.analytics.messaging.KafkaEventType;
import com.energy.analytics.model.entity.FlowPoint;
import com.energy.analytics.model.entity.Metric;
import com.energy.analytics.repository.CrossborderFlowRepository;
import com.energy.analytics.repository.RawMetricRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnergyPersistenceService {

    private final RawMetricRepository metricRepository;
    private final CrossborderFlowRepository flowRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processRawEnergyMetrics(RawEnergyEventDTO payload) {
        List<Metric> entities = payload.data().stream()
                        .filter(dto -> dto.value() != null && !Double.isNaN(dto.value()))
                        .map(dto -> new Metric(
                            Instant.ofEpochSecond(dto.timestamp()),
                            payload.region(),
                            payload.metric(),
                            dto.source(),
                            dto.category() != null ? dto.category() : "actual",
                            dto.value()
                        ))
                        .toList();

        if (entities.isEmpty()) return;

        metricRepository.upsertBatch(entities);

        if (payload.type().equals(KafkaEventType.BACKFILL_METRICS)) return;

        payload.data().stream()
               .map(dto -> Instant.ofEpochSecond(dto.timestamp()))
               .max(Instant::compareTo)
               .ifPresent(latestTs -> eventPublisher.publishEvent(
                       new EnergyMetricsStoredEvent(latestTs, payload.region())
               ));
    }

    @Transactional
    public void processCrossborderFlows(CrossborderFlowEventDTO payload) {
       List<FlowPoint> entities = payload.data().stream()
               .map(dto -> new FlowPoint(
                       Instant.ofEpochSecond(dto.timestamp()),
                       dto.fromRegion(),
                       dto.toRegion(),
                       dto.exportMW(),
                       dto.importMW()
               ))
               .toList();

       if (entities.isEmpty()) return;

       flowRepository.upsertBatch(entities);

       if (payload.type().equals(KafkaEventType.BACKFILL_FLOWS)) return;

       payload.data().stream()
               .map(dto -> Instant.ofEpochSecond(dto.timestamp()))
               .max(Instant::compareTo)
               .ifPresent(latestTs -> eventPublisher.publishEvent(
                       new CrossborderFlowsStoredEvent(latestTs, payload.region())
               ));
    }

}
