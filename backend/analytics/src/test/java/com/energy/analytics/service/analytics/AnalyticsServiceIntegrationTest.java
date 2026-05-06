package com.energy.analytics.service.analytics;
import com.energy.analytics.helpers.EnergyDataFactory;
import com.energy.analytics.model.entity.RawMetric;
import com.energy.analytics.repository.DerivedMetricRepositoryImpl;
import com.energy.analytics.repository.SmoothedMetricRepositoryImpl;
import com.energy.analytics.service.state.GridCacheStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest(classes = {
  AnalyticsService.class,
  GridCacheStore.class,
  AggregateCalculator.class
})
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
  DataSourceAutoConfiguration.class,
  DataSourceTransactionManagerAutoConfiguration.class,
  HibernateJpaAutoConfiguration.class,
  FlywayAutoConfiguration.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AnalyticsServiceIntegrationTest {

   @Autowired
   private AnalyticsService analyticsService;

   @MockitoBean
   private SmoothedMetricRepositoryImpl smoothedMetricRepository;

   @MockitoBean
   private DerivedMetricRepositoryImpl derivedMetricRepository;

   @MockitoSpyBean
   private GridCacheStore gridCacheStore;

   @MockitoSpyBean
   private AggregateCalculator aggregateCalculator;

   @Test
   void shouldShortCircuitWhenWindowIsTooSmall() {
      Instant ts = Instant.parse("2026-05-05T10:00:00Z");

      // Sending only 1 metric (required: 3)
      List<RawMetric> singleMetric = List.of(
              EnergyDataFactory.create(ts.toString(), "solar", 100.0)
      );

      analyticsService.process(singleMetric);

      // Verify no attempt to update snapshot
      verify(gridCacheStore, never()).updateGridSnapshot(any(), any());
   }

   @Test
   void shouldInvokeSnapshotUpdateWhenWindowIsFull() {
      Instant baseTs = Instant.parse("2026-05-05T11:00:00Z");

      // send 3 batches
      for (int i = 0; i < 3; i++) {
         Instant currentEntryTs = baseTs.plus(Duration.ofMillis(i));

         List<RawMetric> batch = EnergyDataFactory.createFullSnapshot(currentEntryTs.toString(), 100.0 + i);

         analyticsService.process(batch);
      }

      verify(smoothedMetricRepository, atLeastOnce()).upsertBatch(anyList());

      verify(gridCacheStore, atLeastOnce()).updateGridSnapshot(any(Instant.class), any());
   }

}
