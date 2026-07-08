import { TimeseriesPoint } from './timeseries-point.model';

export interface DashboardMetricsTimeline {
  region: string;
  generatedAt: string;
  metrics: Record<string, TimeseriesPoint[]>;
}
