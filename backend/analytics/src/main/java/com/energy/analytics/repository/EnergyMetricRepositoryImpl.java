package com.energy.analytics.repository;

import com.energy.analytics.model.EnergyMetric;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EnergyMetricRepositoryImpl implements EnergyMetricRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void upsertBatch(List<EnergyMetric> metrics) {
        String sql = """
            INSERT INTO energy_metrics (
                timestamp, region, metric, source, category, value
            ) VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (timestamp, region, metric, source, category)
            DO UPDATE SET value = EXCLUDED.value
       """;

        jdbcTemplate.batchUpdate(
            sql,
            metrics,
            50,
            (ps, m) -> {
                ps.setObject(1, m.getTimestamp());
                ps.setObject(2, m.getRegion());
                ps.setObject(3, m.getMetric());
                ps.setObject(4, m.getSource());
                ps.setObject(5, m.getCategory());
                ps.setObject(6, m.getValue());
            }
        );
    }
}
