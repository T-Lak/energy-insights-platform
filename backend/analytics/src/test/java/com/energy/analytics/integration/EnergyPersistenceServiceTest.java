package com.energy.analytics.integration;

import com.energy.analytics.BaseContainerTest;
import com.energy.analytics.ingestion.dto.RawEnergyEventDTO;
import com.energy.analytics.ingestion.dto.RawMetricBatchItemDTO;
import com.energy.analytics.ingestion.KafkaEventType;
import com.energy.analytics.ingestion.EnergyPersistenceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Transactional
class EnergyPersistenceServiceTest extends BaseContainerTest {

   @Autowired
   private EnergyPersistenceService persistenceService;

   @Autowired
   private JdbcTemplate jdbcTemplate;

   @MockitoBean
   private ApplicationEventPublisher eventPublisher;

   @Test
   @DisplayName("Should filter out NaN/Null values and batch upsert clean records into TimescaleDB")
   void processRawEnergyMetrics_FiltersCorruptedData() {
      var dataPoints = List.of(
              new RawMetricBatchItemDTO(Instant.now().getEpochSecond(), "solar", "actual aggregated", 450.0),
              new RawMetricBatchItemDTO(Instant.now().getEpochSecond(), "wind onshore", "actual aggregated", Double.NaN),
              new RawMetricBatchItemDTO(Instant.now().getEpochSecond(), "biomass", "actual aggregated", 120.0)
      );
      var payload = new RawEnergyEventDTO(KafkaEventType.LIVE_METRICS, "DE-LU", "generation", dataPoints);

      persistenceService.processRawEnergyMetrics(payload);

      Integer rowCount = jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM energy_metrics WHERE region = ?",
              Integer.class,
              "DE-LU"
      );

      assertThat(rowCount).isEqualTo(2);
   }

   @Test
   @DisplayName("Should return early and skip database updates completely when all values are NaN")
   void processRawEnergyMetrics_HandlesAllNaNValues() {
      var dataPoints = List.of(
              new RawMetricBatchItemDTO(1779942400L, "solar", "actual aggregated", Double.NaN),
              new RawMetricBatchItemDTO(1779946000L, "biomass", "actual aggregated", Double.NaN)
      );
      var payload = new RawEnergyEventDTO(KafkaEventType.LIVE_METRICS, "DE-LU", "generation", dataPoints);

      persistenceService.processRawEnergyMetrics(payload);

      Integer rowCount = jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM energy_metrics WHERE region = ?",
              Integer.class,
              "DE-LU"
      );
      assertThat(rowCount).isZero();
   }

   @Test
   @DisplayName("Should suppress downstream update events when handling historical backfills")
   void processRawEnergyMetrics_SuppressesEventsOnBackfill() {
      var dataPoints = List.of(new RawMetricBatchItemDTO(1779942400L, "solar", "actual", 100.0));
      var payload = new RawEnergyEventDTO(KafkaEventType.BACKFILL_METRICS, "DE-LU", "generation", dataPoints);

      persistenceService.processRawEnergyMetrics(payload);

      verifyNoInteractions(eventPublisher);
   }
}