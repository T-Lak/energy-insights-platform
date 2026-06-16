export enum KpiType {
  RENEWABLE_SHARE = 'renewable_share',
  CARBON_INTENSITY = 'carbon_intensity',
  TOTAL_LOAD = 'total_load',
  NET_BALANCE = 'net_balance',
}

export interface KpiConfig {
  label: string;
  unit: string;
  icon: string;
}

export const KPI_CONFIG: Record<KpiType, KpiConfig> = {
  [KpiType.CARBON_INTENSITY]: {
    label: 'Carbon Intensity',
    unit: 'gCO₂/kWh',
    icon: 'carbon.svg',
  },
  [KpiType.RENEWABLE_SHARE]: {
    label: 'Renewable Share',
    unit: '%',
    icon: 'renewable-share.svg',
  },
  [KpiType.TOTAL_LOAD]: {
    label: 'Total Load',
    unit: 'GW',
    icon: 'load.svg',
  },
  [KpiType.NET_BALANCE]: {
    label: 'Net Balance',
    unit: 'GW',
    icon: 'net-balance.svg',
  },
};
