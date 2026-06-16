import { TimeseriesPointDTO } from './timeseries-point.dto';

export interface KpiSnapshotPayload {
  renewableShare: TimeseriesPointDTO;
  carbonIntensity: TimeseriesPointDTO;
  totalLoad: TimeseriesPointDTO;
  netBalance: TimeseriesPointDTO;
}
