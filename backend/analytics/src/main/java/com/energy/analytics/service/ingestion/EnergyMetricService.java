package com.energy.analytics.service.ingestion;

import com.energy.analytics.dto.RawEnergyEventDTO;
import com.energy.analytics.model.EnergyMetric;
import com.energy.analytics.repository.EnergyMetricRepositoryImpl;
import com.energy.analytics.service.analytics.AnalyticsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnergyMetricService {

    private final EnergyMetricRepositoryImpl repository;
    private final AnalyticsService analyticsService;

    @Transactional
    public void processMetrics(RawEnergyEventDTO payload) {
        Instant ts = Instant.ofEpochSecond(payload.timestamp());

        List<EnergyMetric> entities = payload.data().stream()
                .map(dto -> {
                    Double value = dto.value();

                    if (value == null || value.isNaN()) {
                        log.warn("Invalid value for {} {} at {}", dto.source(), dto.category(), payload.timestamp());
                        return null;
                    }

                    return new EnergyMetric(
                        ts,
                        payload.region(),
                        payload.metric(),
                        dto.source(),
                        dto.category() != null ? dto.category() : "actual",
                        dto.value()
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        repository.upsertBatch(entities);

        analyticsService.process(entities);
    }

}
