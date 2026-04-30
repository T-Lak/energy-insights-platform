package com.energy.analytics.service;

import com.energy.analytics.dto.RawEnergyEventDTO;
import com.energy.analytics.dto.RawMetricDataDTO;
import com.energy.analytics.model.EnergyMetric;
import com.energy.analytics.repository.EnergyMetricRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EnergyMetricServiceTest {

   @Mock
   private EnergyMetricRepositoryImpl repository;

   @InjectMocks
   private EnergyMetricService service;

   @Test
   void shouldMapDtoToEntitiesWithDefaultCategory() {
      long unixTimestamp = 1704067200L; // 2024-01-01T00:00:00Z
      RawEnergyEventDTO payload = new RawEnergyEventDTO(
              "DE_LU",
              "generation",
              unixTimestamp,
              List.of(new RawMetricDataDTO("fossil gas", null, 100.5))
      );

      service.processMetrics(payload);

      ArgumentCaptor<List<EnergyMetric>> captor = ArgumentCaptor.forClass(List.class);
      verify(repository).upsertBatch(captor.capture());

      List<EnergyMetric> sentToRepo = captor.getValue();
      assertThat(sentToRepo).hasSize(1);

      EnergyMetric result = sentToRepo.get(0);
      assertThat(result.getTimestamp()).isEqualTo(Instant.ofEpochSecond(unixTimestamp));
      assertThat(result.getCategory()).isEqualTo("actual"); // Verified default value
      assertThat(result.getValue()).isEqualTo(100.5);
   }
}
