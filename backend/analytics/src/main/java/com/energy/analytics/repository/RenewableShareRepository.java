package com.energy.analytics.repository;

import com.energy.analytics.model.entity.RenewableMix;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RenewableShareRepository {

   private final JdbcTemplate jdbcTemplate;

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

}
