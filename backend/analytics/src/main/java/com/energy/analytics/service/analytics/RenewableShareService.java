package com.energy.analytics.service.analytics;

import com.energy.analytics.model.entity.DailyEnergySummary;
import com.energy.analytics.model.entity.RenewableMix;
import com.energy.analytics.repository.RenewableShareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RenewableShareService {

   private final RenewableShareRepository renewableShareRepository;

   public List<RenewableMix> getRenewablesMixPerDay(Instant startDate, Instant endDate, String region) {
      return renewableShareRepository.getRenewablesMetricsPerDay(startDate, endDate, region);
   }

   public List<DailyEnergySummary> getDailySummary(LocalDate date, String region) {
      return renewableShareRepository.getDailySummary(date, region);
   }



}
