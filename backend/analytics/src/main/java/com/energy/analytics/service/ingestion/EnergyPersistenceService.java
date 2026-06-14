package com.energy.analytics.service.ingestion;

import com.energy.analytics.dto.ingestion.CrossborderFlowEventDTO;
import com.energy.analytics.dto.ingestion.RawEnergyEventDTO;
import com.energy.analytics.event.CrossborderFlowsStoredEvent;
import com.energy.analytics.event.EnergyMetricsStoredEvent;
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
       Instant ts = Instant.ofEpochSecond(payload.timestamp());

       List<Metric> entities = payload.data().stream()
               .filter(dto -> dto.value() != null && !Double.isNaN(dto.value()))
               .map(dto -> new Metric(
                       ts,
                       payload.region(),
                       payload.metric(),
                       dto.source(),
                       dto.category() != null ? dto.category() : "actual",
                       dto.value()
               ))
               .toList();

        metricRepository.upsertBatch(entities);

        eventPublisher.publishEvent(new EnergyMetricsStoredEvent(ts, payload.region()));
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

       flowRepository.upsertBatch(entities);

       eventPublisher.publishEvent(new CrossborderFlowsStoredEvent(
               Instant.ofEpochSecond(payload.timestamp()),
               payload.region()
       ));
    }

}
