package com.energy.analytics.renewables;

import com.energy.analytics.dashboard.dto.EnergyCategoryBreakdownDTO;
import com.energy.analytics.renewables.dto.RenewableSourceMetricsDTO;
import com.energy.analytics.dashboard.model.DailyEnergySummary;
import com.energy.analytics.renewables.dto.RenewableMixPointDTO;
import com.energy.analytics.renewables.model.RenewableMix;
import com.energy.analytics.shared.mapper.RenewablesMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/analytics/renewables")
public class RenewableShareController {

   private final RenewableShareService renewableShareService;

   @GetMapping("/mix/daily")
   public ResponseEntity<List<RenewableMixPointDTO>> getDailyRenewablesMix(
           @RequestParam("date") String date,
           @RequestParam("region") String region
   ) {
      LocalDate localDate;

      try {
         localDate = LocalDate.parse(date);
      } catch (DateTimeParseException e) {
         return ResponseEntity.badRequest().build();
      }

      if (region == null || region.isBlank()) {
         return ResponseEntity.badRequest().build();
      }

      Instant startDate = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
      Instant endDate = localDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

      List<RenewableMix> entities = renewableShareService.getRenewablesMixPerDay(startDate, endDate, region);
      List<RenewableMixPointDTO> dtoList = entities.stream()
              .map(RenewablesMapper::toRenewableMixDTO)
              .toList();

      return ResponseEntity.ok(dtoList);
   }

   @GetMapping("/daily-summary")
   public ResponseEntity<List<EnergyCategoryBreakdownDTO>> getDailySummary(
           @RequestParam("date") String date,
           @RequestParam("region") String region
   ) {
      log.info("Fetch daily summaries");
      LocalDate localDate;

      try {
         localDate = LocalDate.parse(date);
      } catch (DateTimeParseException e) {
         return ResponseEntity.badRequest().build();
      }

      if (region == null || region.isBlank()) {
         return ResponseEntity.badRequest().build();
      }

      List<DailyEnergySummary> summaries = renewableShareService.getDailySummary(localDate, region);
      List<EnergyCategoryBreakdownDTO> dtoList = summaries.stream()
              .map(RenewablesMapper::toDailyEnergySummaryDTO)
              .toList();

      return ResponseEntity.ok(dtoList);
   }

   @GetMapping("/daily-sources")
   public ResponseEntity<List<RenewableSourceMetricsDTO>> getDailySummaries(
           @RequestParam("date") String date,
           @RequestParam("region") String region
   ) {
      log.info("Fetch daily metrics");
      LocalDate localDate;

      try {
         localDate = LocalDate.parse(date);
      } catch (DateTimeParseException e) {
         return ResponseEntity.badRequest().build();
      }

      if (region == null || region.isBlank()) {
         return ResponseEntity.badRequest().build();
      }

      List<RenewableSourceMetricsDTO> summaries = renewableShareService.getDailySources(localDate, region);

      return ResponseEntity.ok(summaries);
   }

}
