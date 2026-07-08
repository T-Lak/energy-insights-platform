package com.energy.analytics.dashboard;

import com.energy.analytics.dashboard.dto.DashboardSummarySnapshot;
import com.energy.analytics.crossborder.dto.LatestFlowsSnapshot;
import com.energy.analytics.dashboard.dto.DashboardLeaderboardOverview;
import com.energy.analytics.crossborder.CrossborderFlowService;
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
   public ResponseEntity<DashboardSummarySnapshot> getLatestKpiData(
           @RequestParam(value = "region", defaultValue = "DE_LU") String region
   ) {
      DashboardSummarySnapshot payload = dashboardService.getLatestKpiSnapshot(region);

      return ResponseEntity.ok(payload);
   }

   @GetMapping("/top-sources")
   public ResponseEntity<DashboardLeaderboardOverview> getTopSources(
           @RequestParam(value = "region", defaultValue = "DE_LU") String region
   ) {
      DashboardLeaderboardOverview payload = dashboardService.getTopSources(region);

      return ResponseEntity.ok(payload);
   }

   @GetMapping("/flows/latest")
   public ResponseEntity<LatestFlowsSnapshot> getLatestFlowPoints(
           @RequestParam(value = "region", defaultValue = "DE_LU") String region
   ) {
      LatestFlowsSnapshot payload = crossborderFlowService.getLatestFlowPoints(region);

      return ResponseEntity.ok(payload);
   }
}
