package com.energy.analytics.service.ingestion;

import com.energy.analytics.dto.ingestion.RawEnergyEventDTO;
import com.energy.analytics.messaging.EnergyMetricsStoredEvent;
import com.energy.analytics.model.entity.Metric;
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
public class EnergyMetricService {

    private final RawMetricRepository repository;
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

        repository.upsertBatch(entities);

        eventPublisher.publishEvent(new EnergyMetricsStoredEvent(ts, payload.region()));
    }

}
