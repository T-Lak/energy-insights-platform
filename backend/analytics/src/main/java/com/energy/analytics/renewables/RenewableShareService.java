package com.energy.analytics.renewables;

import com.energy.analytics.renewables.dto.RenewableSourceMetricsDTO;
import com.energy.analytics.dashboard.model.DailyEnergySummary;
import com.energy.analytics.renewables.model.DailyRenewablesSummary;
import com.energy.analytics.renewables.model.RenewableMix;
import com.energy.analytics.renewables.repository.RenewableShareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

   public List<RenewableSourceMetricsDTO> getDailySources(LocalDate date, String region) {
      OffsetDateTime startOfDay = date.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
      OffsetDateTime bufferStartDate = startOfDay.minusDays(1);
      OffsetDateTime endOfDay = date.atTime(LocalTime.MAX).atZone(ZoneOffset.UTC).toOffsetDateTime();

      List<DailyRenewablesSummary> entities = this.renewableShareRepository
              .getDailyMetrics(bufferStartDate, endOfDay, region);

      List<RenewableSourceMetricsDTO> allSummaries = calculateRenewableSummaries(
              entities,
              1,
              24,
              ChronoUnit.HOURS
      );

      Instant dayCutoff = startOfDay.toInstant();
      return allSummaries.stream()
              .filter(s -> !s.timestamp().isBefore(dayCutoff))
              .collect(Collectors.toList());
   }

   private List<RenewableSourceMetricsDTO> calculateRenewableSummaries(
           List<DailyRenewablesSummary> metrics,
           int shortTermAmount,
           int longTermAmount,
           ChronoUnit unit
   ) {
      List<RenewableSourceMetricsDTO> summaries = new ArrayList<>();

      Map<String, DailyRenewablesSummary> lookupMap = metrics.stream()
              .collect(Collectors.toMap(
                      m -> m.getTimestamp().toString() + "_" + m.getSource(),
                      m -> m,
                      (existing, replacement) -> existing
              ));

      for (DailyRenewablesSummary point : metrics) {
         Instant shortTermPast = point.getTimestamp().minus(shortTermAmount, unit);
         Instant longTermPast = point.getTimestamp().minus(longTermAmount, unit);

         DailyRenewablesSummary past1hPoint = lookupMap.get(shortTermPast.toString() + "_" + point.getSource());
         DailyRenewablesSummary past24hPoint = lookupMap.get(longTermPast.toString() + "_" + point.getSource());

         double change1hPercentage = 0.0;
         if (past1hPoint != null && past1hPoint.getAvgGenerationMW() > 0.0) {
            change1hPercentage = calculatePercentage(point.getAvgGenerationMW(), past1hPoint.getAvgGenerationMW());
         }

         double change24hPercentage = 0.0;
         if (past24hPoint != null && past24hPoint.getAvgGenerationMW() > 0.0) {
            change24hPercentage = calculatePercentage(point.getAvgGenerationMW(), past24hPoint.getAvgGenerationMW());
         }

         summaries.add(new RenewableSourceMetricsDTO(
                 point.getTimestamp(),
                 point.getSource(),
                 point.getRegion(),
                 point.getAvgGenerationMW(),
                 change1hPercentage,
                 change24hPercentage
         ));
      }

      return summaries;
   }

   private double calculatePercentage(double current, double past) {
      if (past == 0.0) {
         if (current > 0.0) return 100.0;
         return 0.0;
      }

      double change = ((current - past) / past) * 100.0;

      return Math.round(change * 10.0) / 10.0;
   }
}
