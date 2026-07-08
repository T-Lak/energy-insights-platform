package com.energy.analytics.shared.domain;

import lombok.Getter;

@Getter
public enum MetricKey {

   RENEWABLE_SHARE("renewable_share"),
   CARBON_INTENSITY("carbon_intensity"),
   TOTAL_LOAD("total_load"),
   NET_BALANCE("net_balance");

   private final String key;

   MetricKey(String key) {
      this.key = key;
   }
}
