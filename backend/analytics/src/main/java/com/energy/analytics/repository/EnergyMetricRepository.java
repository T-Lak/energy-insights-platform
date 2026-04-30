package com.energy.analytics.repository;

import com.energy.analytics.model.EnergyMetric;
import com.energy.analytics.model.MetricId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnergyMetricRepository extends JpaRepository<EnergyMetric, MetricId> {

}
