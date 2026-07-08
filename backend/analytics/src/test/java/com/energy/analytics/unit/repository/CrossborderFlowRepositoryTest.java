package com.energy.analytics.unit.repository;

import com.energy.analytics.BaseContainerTest;
import com.energy.analytics.crossborder.dto.FlowGridEdgeDTO;
import com.energy.analytics.crossborder.dto.FlowTotalsDTO;
import com.energy.analytics.crossborder.model.FlowPoint;
import com.energy.analytics.crossborder.repository.CrossborderFlowRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CrossborderFlowRepositoryTest extends BaseContainerTest {

   @Autowired
   private CrossborderFlowRepository repository;

   @Autowired
   private JdbcTemplate jdbcTemplate;

   private final Instant now = Instant.parse("2026-07-08T12:00:00Z");
   private final Instant anHourAgo = now.minusSeconds(3600);

   @BeforeEach
   void setUp() {
      jdbcTemplate.execute("""
        DO $$
        BEGIN
            IF NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'uq_crossborder_flows_timestamp_region'
            ) THEN
                ALTER TABLE crossborder_flows
                ADD CONSTRAINT uq_crossborder_flows_timestamp_region
                UNIQUE ("timestamp", from_region, to_region);
            END IF;
        END $$;
        """);

      jdbcTemplate.execute("DELETE FROM crossborder_flows");
   }

   @AfterEach
   void cleanUp() {
      jdbcTemplate.execute("DELETE FROM crossborder_flows");
   }

   @BeforeAll
   static void setupSchema(@Autowired JdbcTemplate jdbcTemplate) {
      jdbcTemplate.execute("""
      DO $$
         BEGIN
             IF NOT EXISTS (
                 SELECT 1 FROM pg_constraint 
                 WHERE conname = 'uq_crossborder_flows_timestamp_region'
             ) THEN
                 ALTER TABLE crossborder_flows
                 ADD CONSTRAINT uq_crossborder_flows_timestamp_region
                 UNIQUE ("timestamp", from_region, to_region);
             END IF;
         END $$;
      """);
   }

   @Test
   @DisplayName("Should parse latest flow points utilizing string splits and max timestamp subqueries")
   void getLatestFlowPoints_ValidatesNativeSqlAndSplits() {
      List<FlowPoint> testFlows = List.of(
              new FlowPoint(anHourAgo, "DE_LU", "FR", 100.0f, 50.0f),
              new FlowPoint(now, "DE_LU", "FR", 500.0f, 200.0f),
              new FlowPoint(now, "DE_LU", "NL", 300.0f, 100.0f)
      );

      repository.upsertBatch(testFlows);

      List<FlowGridEdgeDTO> results = repository.getLatestFlowPoints("DE_LU");

      assertThat(results).hasSize(2);

      FlowGridEdgeDTO frFlow = results.stream().filter(f -> f.toRegion().equals("FR")).findFirst().orElseThrow();
      assertThat(frFlow.timestamp()).isEqualTo(now);
      assertThat(frFlow.exportMW()).isEqualTo(500.0f);
      assertThat(frFlow.importMW()).isEqualTo(200.0f);

      FlowGridEdgeDTO nlFlow = results.stream().filter(f -> f.toRegion().equals("NL")).findFirst().orElseThrow();
      assertThat(nlFlow.toRegion()).isEqualTo("NL");
   }

   @Test
   @DisplayName("Should accurately aggregate range boundaries for net totals")
   void getFlowTotals_CalculatesNetBalancesAcrossTimeWindow() {
      List<FlowPoint> testFlows = List.of(
              new FlowPoint(now, "DE", "FR", 400.0f, 100.0f),
              new FlowPoint(now, "DE", "NL", 200.0f, 50.0f)
      );
      repository.upsertBatch(testFlows);

      List<FlowTotalsDTO> totals = repository.getFlowTotals(now.minusSeconds(10), now.plusSeconds(10), "DE");

      assertThat(totals).hasSize(1);
      FlowTotalsDTO entry = totals.get(0);
      assertThat(entry.totalExportMW()).isEqualTo(600.0f);
      assertThat(entry.totalImportMW()).isEqualTo(150.0f);

      assertThat(entry.netExchangeMW()).isEqualTo(450.0f);
   }
}
