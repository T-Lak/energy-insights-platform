package com.energy.analytics.controller;

import com.energy.analytics.dto.rest.RenewableMixDTO;
import com.energy.analytics.model.entity.RenewableMix;
import com.energy.analytics.model.mapper.RenewablesMapper;
import com.energy.analytics.service.analytics.RenewableShareService;
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

   @GetMapping("mix/daily")
   public ResponseEntity<List<RenewableMixDTO>> getDailyRenewablesMix(
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
      List<RenewableMixDTO> dtoList = entities.stream()
              .map(RenewablesMapper::toDTO)
              .toList();

      return ResponseEntity.ok(dtoList);
   }

}
