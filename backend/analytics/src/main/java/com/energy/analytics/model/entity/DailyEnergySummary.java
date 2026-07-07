package com.energy.analytics.model.entity;

import com.energy.analytics.dto.rest.MetricPointDTO;
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
   private List<MetricPointDTO> metricPoints;
}
