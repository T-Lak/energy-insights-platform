package com.energy.analytics.crossborder;

import com.energy.analytics.crossborder.dto.RegionalFlowTotalsTimeline;
import com.energy.analytics.crossborder.dto.CrossborderFlowsOverview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/analytics/flows")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class CrossborderFlowController {

   private final CrossborderFlowService crossborderFlowService;

   public CrossborderFlowController(CrossborderFlowService crossborderFlowService) {
      this.crossborderFlowService = crossborderFlowService;
   }

   @GetMapping("/timeseries")
   public ResponseEntity<RegionalFlowTotalsTimeline> getFlowTotalsTimeSeries(
           @RequestParam(value = "hours") int hours,
           @RequestParam(value = "region", defaultValue = "DE_LU") String region
   ) {
      RegionalFlowTotalsTimeline payload = crossborderFlowService.getFlowTotalsTimeSeries(hours, region);

      return ResponseEntity.ok(payload);
   }

   @GetMapping("/daily")
   public ResponseEntity<CrossborderFlowsOverview> getDailyFlows(
           @RequestParam(value = "endDate", required = false) Instant endDate,
           @RequestParam(value = "region", defaultValue = "DE_LU") String region
   ) {
      log.info("Fetching daily crossborder flows.");
      Instant date = (endDate != null) ? endDate : Instant.now();

      return ResponseEntity.ok(crossborderFlowService.getDailyFlows(date, region));
   }

   @GetMapping("/weekly")
   public ResponseEntity<CrossborderFlowsOverview> getWeeklyFlows(
           @RequestParam(value = "endDate", required = false) Instant endDate,
           @RequestParam(value = "region", defaultValue = "DE_LU") String region
   ) {
      log.info("Fetching weekly crossborder flows.");
      Instant date = (endDate != null) ? endDate : Instant.now();

      return ResponseEntity.ok(crossborderFlowService.getWeeklyFlows(date, region));
   }

   @GetMapping("/monthly")
   public ResponseEntity<CrossborderFlowsOverview> getMonthlyFlows(
           @RequestParam(value = "endDate", required = false) Instant endDate,
           @RequestParam(value = "region", defaultValue = "DE_LU") String region
   ) {
      log.info("Fetching monthly crossborder flows.");
      Instant date = (endDate != null) ? endDate : Instant.now();

      return ResponseEntity.ok(crossborderFlowService.getMonthlyFlows(date, region));
   }

}
