package com.energy.analytics.controller;

import com.energy.analytics.dto.rest.KpiSnapshotPayload;
import com.energy.analytics.dto.rest.LatestFlowsPayload;
import com.energy.analytics.dto.rest.SourceRankingPayload;
import com.energy.analytics.service.analytics.CrossborderFlowService;
import com.energy.analytics.service.analytics.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics/metrics")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

   private final DashboardService dashboardService;
   private final CrossborderFlowService crossborderFlowService;

   public DashboardController(
           DashboardService dashboardService,
           CrossborderFlowService crossborderFlowService
   ) {
      this.dashboardService = dashboardService;
      this.crossborderFlowService = crossborderFlowService;
   }

   @GetMapping("/kpi/latest")
   public ResponseEntity<KpiSnapshotPayload> getLatestKpiData(
           @RequestParam(value = "region", defaultValue = "DE_LU") String region
   ) {
      KpiSnapshotPayload payload = dashboardService.getLatestKpiSnapshot(region);

      return ResponseEntity.ok(payload);
   }

   @GetMapping("/top-sources")
   public ResponseEntity<SourceRankingPayload> getTopSources(
           @RequestParam(value = "region", defaultValue = "DE_LU") String region
   ) {
      SourceRankingPayload payload = dashboardService.getTopSources(region);

      return ResponseEntity.ok(payload);
   }

   @GetMapping("/flows/latest")
   public ResponseEntity<LatestFlowsPayload> getLatestFlowPoints(
           @RequestParam(value = "region", defaultValue = "DE_LU") String region
   ) {
      LatestFlowsPayload payload = crossborderFlowService.getLatestFlowPoints(region);

      return ResponseEntity.ok(payload);
   }
}
