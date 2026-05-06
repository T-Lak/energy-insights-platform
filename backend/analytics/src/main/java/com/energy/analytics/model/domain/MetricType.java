package com.energy.analytics.model.domain;

public enum MetricType {

   GENERATION("generation"),
   LOAD("load");

   private final String type;

   MetricType(String metricType) {
      this.type = metricType;
   }

}
