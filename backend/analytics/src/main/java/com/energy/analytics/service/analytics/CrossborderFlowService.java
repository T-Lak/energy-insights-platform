package com.energy.analytics.service.analytics;

import com.energy.analytics.dto.rest.CountryFlowSummaryDTO;
import com.energy.analytics.dto.rest.CrossborderFlowsPayload;
import com.energy.analytics.dto.rest.CrossborderFlowTotalsTsPayload;
import com.energy.analytics.dto.rest.LatestFlowsPayload;
import com.energy.analytics.dto.websocket.model.FlowPointDTO;
import com.energy.analytics.dto.websocket.model.FlowTotalsDTO;
import com.energy.analytics.model.domain.GranularityView;
import com.energy.analytics.repository.CrossborderFlowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

record AggregatedFlowsResult(
     List<FlowPointDTO> flowPoints,
     List<FlowTotalsDTO> flowTotals
) {}

@Service
@Slf4j
@RequiredArgsConstructor
public class CrossborderFlowService {

   private final CrossborderFlowRepository crossborderFlowRepository;

   public CrossborderFlowTotalsTsPayload getFlowTotalsTimeSeries(int hours, String region) {
      if (hours <= 0 || hours > 24) {
         throw new IllegalArgumentException("hours must be between 1 and 24");
      }

      log.info("fetching latest flow timeseries data for region: {}", region);

      Instant end = Instant.now();
      Instant start = end.minus(hours, ChronoUnit.HOURS);

      List<FlowTotalsDTO> flowTotals = crossborderFlowRepository.getFlowTotals(start, end, region);

      return new CrossborderFlowTotalsTsPayload(region, Instant.now(), flowTotals);
   }

   public LatestFlowsPayload getLatestFlowPoints(String region) {
      log.info("Fetching latest flow points for region: {}", region);

      List<FlowPointDTO> flowPoints = crossborderFlowRepository.getLatestFlowPoints(region);

      if (flowPoints == null) {
         flowPoints = Collections.emptyList();
      }

      return new LatestFlowsPayload(flowPoints);
   }

   public CrossborderFlowsPayload getDailyFlows(Instant endDate, String region) {
      if (endDate.isAfter(Instant.now())) {
         throw new IllegalArgumentException("End date must not be in the future.");
      }

      log.info("Fetching daily flow points for region: {}", region);

      // Fetch two weeks, with the first week being a buffer to calculate percentages
      Instant startDate = endDate.minus(24, ChronoUnit.HOURS);
      Instant bufferStartDate = endDate.minus(48, ChronoUnit.HOURS);

      return this.getFlows(
              GranularityView.HOURLY,
              bufferStartDate,
              startDate,
              endDate,
              region, 1,
              24,
              ChronoUnit.HOURS
      );
   }

   public CrossborderFlowsPayload getWeeklyFlows(Instant endDate, String region) {
      if (endDate.isAfter(Instant.now())) {
         throw new IllegalArgumentException("End date must not be in the future.");
      }

      log.info("Fetching weekly flow points for region: {}", region);

      // Fetch two weeks, with the first week being a buffer to calculate percentages
      Instant startDate = endDate.minus(7, ChronoUnit.DAYS);
      Instant bufferStartDate = endDate.minus(14, ChronoUnit.DAYS);

      return this.getFlows(
              GranularityView.WEEKLY,
              bufferStartDate,
              startDate,
              endDate,
              region,
              1,
              7,
              ChronoUnit.DAYS
      );
   }

   public CrossborderFlowsPayload getMonthlyFlows(Instant endDate, String region) {
      if (endDate.isAfter(Instant.now())) {
         throw new IllegalArgumentException("End date must not be in the future.");
      }

      log.info("Fetching monthly flow points for region: {}", region);

      // Fetch two weeks, with the first week being a buffer to calculate percentages
      Instant startDate = endDate.minus(30, ChronoUnit.DAYS);
      Instant bufferStartDate = endDate.minus(60, ChronoUnit.DAYS);

      return this.getFlows(
              GranularityView.MONTHLY,
              bufferStartDate,
              startDate,
              endDate,
              region,
              7,
              30,
              ChronoUnit.DAYS
      );
   }

   public CrossborderFlowsPayload getFlows(
           GranularityView granularityView,
           Instant bufferStartDate,
           Instant startDate,
           Instant endDate,
           String region,
           int shortTerm,
           int longTerm,
           ChronoUnit unit
   ) {
      List<FlowPointDTO> allFlows = (granularityView == GranularityView.HOURLY
              || granularityView == GranularityView.WEEKLY)
              ? crossborderFlowRepository.getWeeklyFlows(bufferStartDate, endDate, region)
              : crossborderFlowRepository.getMonthlyFlows(bufferStartDate, endDate, region);

      if (allFlows == null || allFlows.isEmpty()) {
         return new CrossborderFlowsPayload(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
      }

      List<CountryFlowSummaryDTO> allSummaries = this.calculateCountrySummaries(
              allFlows,
              shortTerm,
              longTerm,
              unit
      );

      List<FlowPointDTO> filteredFlows = allFlows.stream()
              .filter(weeklyFlow -> !weeklyFlow.timestamp().isBefore(startDate))
              .toList();

      AggregatedFlowsResult aggregatedFlows = this.aggregateFlows(filteredFlows);

      List<CountryFlowSummaryDTO> filteredSummaries = allSummaries.stream()
              .filter(summary -> !summary.timestamp().isBefore(startDate))
              .toList();

      return new CrossborderFlowsPayload(
              aggregatedFlows.flowPoints(),
              aggregatedFlows.flowTotals(),
              filteredSummaries
      );
   }

   /**
    * Calculates the summaries by country, including short-term percentages (1h for daily, 1 day for weekly, etc.),
    * and long-term percentage changes (24h for daily, 7 days for weekly, etc.).
    * @param flows List of flow points.
    * @param shortTermDays short-term range (e.g. 1h)
    * @param longTermDays long-term range (e.g. 7 days)
    * @param unit The unit (hours, days, etc.) used to calculate the lookup into the past
    * @return Summaries per country code.
    */
   private List<CountryFlowSummaryDTO> calculateCountrySummaries(
           List<FlowPointDTO> flows,
           int shortTermDays,
           int longTermDays,
           ChronoUnit unit
   ) {
      List<CountryFlowSummaryDTO> summaries = new ArrayList<>();

      Map<String, FlowPointDTO> lookupMap = flows.stream()
              .collect(Collectors.toMap(
                      flowPoint -> flowPoint.timestamp().toString() + "_" + flowPoint.toRegion(),
                      flowPoint -> flowPoint,
                      (existing, replacement) -> existing
              ));

      for (FlowPointDTO flowPoint : flows) {
         Instant shortTermPast = flowPoint.timestamp().minus(shortTermDays, unit);
         Instant longTermPast = flowPoint.timestamp().minus(longTermDays, unit);

         FlowPointDTO pastDayPoint = lookupMap.get(shortTermPast.toString() + "_" + flowPoint.toRegion());
         FlowPointDTO pastWeekPoint = lookupMap.get(longTermPast.toString() + "_" + flowPoint.toRegion());

         double importShortTermPercentage = 0.0;
         if (pastDayPoint != null && pastDayPoint.importMW() > 0.0) {
            importShortTermPercentage = calculatePercentage(flowPoint.importMW(), pastDayPoint.importMW());
         }

         double exportShortTermPercentage = 0.0;
         if (pastDayPoint != null && pastDayPoint.exportMW() > 0.0) {
            exportShortTermPercentage = calculatePercentage(flowPoint.exportMW(), pastDayPoint.exportMW());
         }

         double importLongTermPercentage = 0.0;
         if (pastWeekPoint != null && pastWeekPoint.importMW() > 0.0) {
            importLongTermPercentage = calculatePercentage(flowPoint.importMW(), pastWeekPoint.importMW());
         }

         double exportLongTermPercentage = 0.0;
         if (pastWeekPoint != null && pastWeekPoint.exportMW() > 0.0) {
            exportLongTermPercentage = calculatePercentage(flowPoint.exportMW(), pastWeekPoint.exportMW());
         }

         float netFlow = flowPoint.exportMW() - flowPoint.importMW();

         CountryFlowSummaryDTO summary = new CountryFlowSummaryDTO(
              flowPoint.timestamp(),
              flowPoint.toRegion(),
              flowPoint.importMW(),
              flowPoint.exportMW(),
              netFlow,
              importShortTermPercentage,
              exportShortTermPercentage,
              importLongTermPercentage,
              exportLongTermPercentage
         );

         summaries.add(summary);
      }

      return summaries;
   }

   private double calculatePercentage(double current, double previous) {
      return ((current - previous) / previous) * 100.00;
   }

   /**
    * Aggregates flow points once grouped by country, and once grouped by timestamp.
    * Country grouping is needed to merge those region codes that belong to the same country (e.g. DK_1, DK_2).
    * Hourly grouping is done to calculate the flow totals by timestamp.
    * @param flows List of flow points.
    * @return The aggregation results.
    */
   private AggregatedFlowsResult aggregateFlows(List<FlowPointDTO> flows) {
      Map<String, FlowPointDTO> countryGroupMap = new LinkedHashMap<>();
      Map<Instant, FlowTotalsDTO> hourlyTotalMap = new LinkedHashMap<>();

      for (FlowPointDTO flow : flows) {
         String strippedRegionCode = flow.toRegion().replaceAll("_[0-9]+$", "");
         String countryGroupKey = flow.timestamp() + "_" + strippedRegionCode;

         countryGroupMap.compute(countryGroupKey, (key, existingPoint) -> {
            if (existingPoint != null) {
               return new FlowPointDTO(
                       existingPoint.timestamp(),
                       existingPoint.fromRegion(),
                       strippedRegionCode,
                       existingPoint.exportMW() + flow.exportMW(),
                       existingPoint.importMW() + flow.importMW()
               );
            } else {
               return new FlowPointDTO(
                       flow.timestamp(),
                       flow.fromRegion(),
                       strippedRegionCode,
                       flow.exportMW(),
                       flow.importMW()
               );
            }
         });

         hourlyTotalMap.compute(flow.timestamp(), (key, existingPoint) -> {
            if (existingPoint != null) {
               return new FlowTotalsDTO(
                       existingPoint.timestamp(),
                       existingPoint.region(),
                       existingPoint.totalExportMW() + flow.exportMW(),
                       existingPoint.totalImportMW() + flow.importMW(),
                       existingPoint.netExchangeMW() + (flow.exportMW() - flow.importMW())
               );
            } else {
               return new FlowTotalsDTO(
                       flow.timestamp(),
                       flow.fromRegion(),
                       flow.exportMW(),
                       flow.importMW(),
                       flow.exportMW() - flow.importMW()
               );
            }
      });
      }
      return new AggregatedFlowsResult(
           countryGroupMap.values().stream().toList(),
           hourlyTotalMap.values().stream().toList()
      );
   }

}
