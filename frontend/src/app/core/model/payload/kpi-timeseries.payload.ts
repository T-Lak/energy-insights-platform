import { TimeseriesPointDTO } from '../dto/timeseries-point.dto';

export interface KpiTimeseriesPayload {
  region: string;
  generatedAt: string;
  metrics: Record<string, TimeseriesPointDTO[]>;
}
