package com.energy.analytics.dashboard.model;

import com.energy.analytics.dashboard.dto.SourceContributionPointDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyEnergySummary {
   private String timePeriod;
   private String category;
   private double amount;
   private List<SourceContributionPointDTO> metricPoints;
}
