package com.energy.analytics.repository;

import com.energy.analytics.model.entity.DailyEnergySummary;
import com.energy.analytics.dto.rest.MetricPointDTO;
import com.energy.analytics.model.entity.RenewableMix;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RenewableShareRepository {

   private final JdbcTemplate jdbcTemplate;

   private final ObjectMapper objectMapper = new ObjectMapper();

   public List<RenewableMix> getRenewablesMetricsPerDay(Instant startDate, Instant endDate, String region) {
      String sql = """
         SELECT
           timestamp,
           SUM(CASE WHEN source = 'solar' THEN value ELSE 0 END) AS solar,
           SUM(CASE WHEN source = 'wind onshore' THEN value ELSE 0 END) AS wind_onshore,
           SUM(CASE WHEN source = 'wind offshore' THEN value ELSE 0 END) AS wind_offshore,
           SUM(CASE WHEN source = 'biomass' THEN value ELSE 0 END) AS biomass,
           SUM(CASE WHEN source IN (
             'hydro run-of-river and poundage',
             'hydro water reservoir'
           ) THEN value ELSE 0 END) AS hydro,
           SUM(CASE WHEN source = 'geothermal' THEN value ELSE 0 END) AS geothermal,
           SUM(CASE WHEN source = 'other renewable' THEN value ELSE 0 END) AS other_renewable
         FROM energy_metrics
         WHERE timestamp BETWEEN ? AND ?
          AND region = ?
          AND category IN ('actual', 'actual aggregated')
         GROUP BY timestamp
         ORDER BY timestamp ASC;
      """;

      return jdbcTemplate.query(
              sql,
              (rs, rowNum) -> new RenewableMix(
                   rs.getTimestamp("timestamp").toInstant(),
                   rs.getDouble("solar"),
                   rs.getDouble("wind_onshore"),
                   rs.getDouble("wind_offshore"),
                   rs.getDouble("biomass"),
                   rs.getDouble("hydro"),
                   rs.getDouble("geothermal"),
                   rs.getDouble("other_renewable")
              ),
              startDate.atOffset(ZoneOffset.UTC),
              endDate.atOffset(ZoneOffset.UTC),
              region
      );
   }

   public List<DailyEnergySummary> getDailySummary(LocalDate date, String region) {
      String sql = """
      SELECT
        CASE
          WHEN time_period = 'Day' THEN 'Day (06:00 - 22:00)'
          WHEN time_period = 'Night' THEN 'Night (22:00 - 06:00)'
          ELSE time_period
        END AS "timePeriod",
        CASE
          WHEN precise_source IN ('waste', 'other', 'hydro pumped storage') THEN 'Other'
          ELSE energy_category
        END AS "category",
        ROUND(SUM(total_mwh)) AS "amount",
        json_agg(json_build_object(
           'source', INITCAP(precise_source),
           'percentage', ROUND(total_mwh)
        ) ORDER BY total_mwh DESC) AS "mixBreakdown"
      FROM view_energy_metrics_daily_summary
      WHERE production_day::date = ?::date
        AND region = ?
        AND energy_category IN ('Renewables', 'Fossils')
      GROUP BY 1, 2;
   """;

      return jdbcTemplate.query(
              sql,
              (rs, rowNum) -> new DailyEnergySummary(
                      rs.getString("timePeriod"),
                      rs.getString("category"),
                      rs.getDouble("amount"),
                      mapJsonToList(rs, "mixBreakdown")
              ),
              date,
              region
      );
   }

   private List<MetricPointDTO> mapJsonToList(ResultSet rs, String columnName) throws SQLException {
      try {
         String jsonString = rs.getString(columnName);
         if (jsonString == null) return List.of();

         return objectMapper.readValue(jsonString, new TypeReference<>() {
         });
      } catch (Exception e) {
         throw new SQLException("Failed to parse mixBreakdown JSON payload", e);
      }
   }

}
