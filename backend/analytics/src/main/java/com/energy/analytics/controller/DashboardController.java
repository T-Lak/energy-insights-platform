package com.energy.analytics.controller;

import com.energy.analytics.dto.rest.KpiSnapshotPayload;
import com.energy.analytics.service.analytics.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics/metrics")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

   private final DashboardService dashboardService;

   public DashboardController(DashboardService dashboardService) {
      this.dashboardService = dashboardService;
   }

   @GetMapping("/kpi/latest")
   public ResponseEntity<KpiSnapshotPayload> getLatestKpiData(
           @RequestParam(value = "region", defaultValue = "DE_LU") String region
   ) {
      KpiSnapshotPayload payload = dashboardService.getLatestKpiSnapshot(region);

      return ResponseEntity.ok(payload);
   }
}
