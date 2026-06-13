package com.energy.analytics.repository;

import com.energy.analytics.model.entity.Metric;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.ZoneOffset;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RawMetricRepository implements BatchRepository<Metric> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void upsertBatch(List<Metric> metrics) {
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
                ps.setObject(1, m.getTimestamp().atOffset(ZoneOffset.UTC));
                ps.setObject(2, m.getRegion());
                ps.setObject(3, m.getMetric());
                ps.setObject(4, m.getSource());
                ps.setObject(5, m.getCategory());
                ps.setObject(6, m.getValue());
            }
        );
    }
}
