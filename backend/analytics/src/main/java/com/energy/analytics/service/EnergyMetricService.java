package com.energy.analytics.service;

import com.energy.analytics.dto.RawEnergyEventDTO;
import com.energy.analytics.model.EnergyMetric;
import com.energy.analytics.repository.EnergyMetricRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnergyMetricService {

    private final EnergyMetricRepository repository;

    @Transactional
    public void processMetrics(RawEnergyEventDTO payload) {
        Instant ts = Instant.ofEpochSecond(payload.timestamp());

        List<EnergyMetric> entities = payload.data().stream()
                .map(dto -> new EnergyMetric(
                        ts,
                        payload.region(),
                        payload.metric(),
                        dto.source(),
                        dto.category() != null ? dto.category() : "actual",
                        dto.value()
                ))
                .toList();

        repository.saveAll(entities);
    }

}
