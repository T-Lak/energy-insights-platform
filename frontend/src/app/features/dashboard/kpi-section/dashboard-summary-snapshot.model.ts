import { TimeseriesPoint } from '../models/timeseries-point.model';

export interface DashboardSummarySnapshot {
  renewableShare: TimeseriesPoint;
  carbonIntensity: TimeseriesPoint;
  totalLoad: TimeseriesPoint;
  netBalance: TimeseriesPoint;
}
