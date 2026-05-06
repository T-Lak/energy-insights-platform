package com.energy.analytics.service;

import com.energy.analytics.dto.RawEnergyEventDTO;
import com.energy.analytics.dto.RawMetricDataDTO;
import com.energy.analytics.model.entity.RawMetric;
import com.energy.analytics.repository.RawMetricRepositoryImpl;
import com.energy.analytics.service.analytics.AnalyticsService;
import com.energy.analytics.service.ingestion.EnergyMetricService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RawMetricServiceTest {

   @Mock
   private RawMetricRepositoryImpl repository;

   @Mock
   private AnalyticsService analyticsService;

   @InjectMocks
   private EnergyMetricService energyMetricService;

   @Test
   void shouldFilterOutNaNValuesAndProcessValidOnes() {
      // Arrange: One valid value (100.0) and one NaN
      RawEnergyEventDTO payload = new RawEnergyEventDTO(
              "DE_LU", "generation", 1704067200L,
              List.of(
                      new RawMetricDataDTO("solar", "actual", 100.0),
                      new RawMetricDataDTO("wind", "actual", Double.NaN)
              )
      );

      energyMetricService.processMetrics(payload);

      ArgumentCaptor<List<RawMetric>> captor = ArgumentCaptor.forClass(List.class);
      verify(repository).upsertBatch(captor.capture());

      List<RawMetric> savedMetrics = captor.getValue();
      assertThat(savedMetrics).hasSize(1);
      assertThat(savedMetrics.get(0).getSource()).isEqualTo("solar");

      verify(analyticsService).process(savedMetrics);
   }

   @Test
   void shouldMapDtoToEntitiesWithDefaultCategory() {
      long unixTimestamp = 1704067200L; // 2024-01-01T00:00:00Z
      RawEnergyEventDTO payload = new RawEnergyEventDTO(
              "DE_LU",
              "generation",
              unixTimestamp,
              List.of(new RawMetricDataDTO("fossil gas", null, 100.5))
      );

      energyMetricService.processMetrics(payload);

      ArgumentCaptor<List<RawMetric>> captor = ArgumentCaptor.forClass(List.class);
      verify(repository).upsertBatch(captor.capture());

      verify(analyticsService).process(anyList());

      List<RawMetric> sentToRepo = captor.getValue();
      assertThat(sentToRepo).hasSize(1);

      RawMetric result = sentToRepo.get(0);
      assertThat(result.getTimestamp()).isEqualTo(Instant.ofEpochSecond(unixTimestamp));
      assertThat(result.getCategory()).isEqualTo("actual"); // Verified default value
      assertThat(result.getValue()).isEqualTo(100.5);
   }
}
