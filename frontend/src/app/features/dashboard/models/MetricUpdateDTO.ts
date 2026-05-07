import { DataPointDTO } from './DataPointDTO';

export interface MetricUpdateDTO {
  region: string;
  generatedAt: string;
  metrics: Record<string, DataPointDTO[]>;
}
