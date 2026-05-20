export enum KpiType {
  RENEWABLE_SHARE = 'renewableShare',
  CARBON_INTENSITY = 'carbonIntensity',
  TOTAL_LOAD = 'totalLoad',
  NET_BALANCE = 'netBalance',
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
    unit: 'MW',
    icon: 'load.svg',
  },
  [KpiType.NET_BALANCE]: {
    label: 'Net Balance',
    unit: 'MW',
    icon: 'net-balance.svg',
  },
};
