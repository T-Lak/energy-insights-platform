package com.energy.analytics.unit.repository;

import com.energy.analytics.BaseContainerTest;
import com.energy.analytics.ingestion.model.Metric;
import com.energy.analytics.ingestion.repository.RawMetricRepository;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

public class RawMetricRepositoryTest extends BaseContainerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RawMetricRepository repository;

    static String region = "DE_LU";

    static Stream<Arguments> provideDistinctMetrics() {
        return Stream.of(
            Arguments.of(List.of(
                new Metric(
                    Instant.parse("2026-01-01T10:00:00Z"),
                    region,
                    "generation",
                    "fossil gas",
                    "actual aggregated",
                    100.3
                ),
                new Metric(
                    Instant.parse("2026-01-01T10:15:00Z"),
                    region,
                    "generation",
                    "fossil gas",
                    "actual aggregated",
                    57.
                ),
                new Metric(
                    Instant.parse("2026-01-01T10:00:00Z"),
                    region,
                    "generation",
                    "biomass",
                    "actual aggregated",
                    174.1
                ),
                new Metric(
                    Instant.parse("2026-01-01T10:00:00Z"),
                    region,
                    "generation",
                    "biomass",
                    "actual consumption",
                    174.1
                )
            ))
        );
    }

    static Stream<Arguments> provideEqualMetrics() {
        return Stream.of(
            Arguments.of(List.of(
                new Metric(
                    Instant.parse("2026-01-01T10:00:00Z"),
                    region,
                    "generation",
                    "fossil gas",
                    "actual aggregated",
                    100.3
                ),
                new Metric(
                    Instant.parse("2026-01-01T10:00:00Z"),
                    region,
                    "generation",
                    "fossil gas",
                    "actual aggregated",
                    110.2
                ),
                new Metric(
                    Instant.parse("2026-01-01T10:00:00Z"),
                    region,
                    "generation",
                    "biomass",
                    "actual aggregated",
                    174.1
                ),
                new Metric(
                    Instant.parse("2026-01-01T10:00:00Z"),
                    region,
                    "generation",
                    "biomass",
                    "actual aggregated",
                    174.
                )
            ))
        );
    }

    @BeforeEach
    void clearDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE energy_metrics");
    }

    @ParameterizedTest
    @MethodSource("provideDistinctMetrics")
    @Transactional
    void testShouldStoreWithoutUpsert(List<Metric> metrics) {
        repository.upsertBatch(metrics);

        List<Metric> fetched = jdbcTemplate.query(
            "SELECT * FROM energy_metrics",
            (rs, rowNum) -> new Metric(
                rs.getTimestamp("timestamp").toInstant(),
                rs.getString("region"),
                rs.getString("metric"),
                rs.getString("source"),
                rs.getString("category"),
                rs.getDouble("value")
            )
        );

        assertThat(fetched)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(metrics);
    }

    @ParameterizedTest
    @MethodSource("provideEqualMetrics")
    @Transactional
    void shouldUpsertRowIfKeyMatches(List<Metric> metrics) {
        repository.upsertBatch(metrics);

        Integer totalRows = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM energy_metrics", Integer.class);
        assertThat(totalRows).isEqualTo(2);

        List<Map<String, Object>> results = jdbcTemplate.queryForList(
            "SELECT source, value FROM energy_metrics"
        );

        assertThat(results).hasSize(2).extracting(
            m -> m.get("source"),
            m -> m.get("value")
        ).containsExactlyInAnyOrder(
            tuple("fossil gas", 110.2),
            tuple("biomass", 174.0)
        );
    }
}
