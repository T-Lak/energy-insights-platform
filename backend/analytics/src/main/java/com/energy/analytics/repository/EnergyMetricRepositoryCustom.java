package com.energy.analytics.repository;

import com.energy.analytics.model.EnergyMetric;

import java.util.List;

public interface EnergyMetricRepositoryCustom {
    void upsertBatch(List<EnergyMetric> metrics);
}
