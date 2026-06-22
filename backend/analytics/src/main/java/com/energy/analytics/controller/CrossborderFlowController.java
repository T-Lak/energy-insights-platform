package com.energy.analytics.controller;

import com.energy.analytics.dto.rest.CrossborderFlowTotalsTsPayload;
import com.energy.analytics.service.analytics.CrossborderFlowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics/flows")
@CrossOrigin(origins = "http://localhost:4200")
public class CrossborderFlowController {

   private final CrossborderFlowService crossborderFlowService;

   public CrossborderFlowController(CrossborderFlowService crossborderFlowService) {
      this.crossborderFlowService = crossborderFlowService;
   }

   @GetMapping("/timeseries")
   public ResponseEntity<CrossborderFlowTotalsTsPayload> getFlowTotalsTimeSeries(
           @RequestParam(value = "hours") int hours,
           @RequestParam(value = "region", defaultValue = "DE_LU") String region
   ) {
      CrossborderFlowTotalsTsPayload payload = crossborderFlowService.getFlowTotalsTimeSeries(hours, region);

      return ResponseEntity.ok(payload);
   }

}
