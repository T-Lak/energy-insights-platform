import { DataPointDTO } from './data-point.dto';

export interface MetricUpdateDTO {
  region: string;
  generatedAt: string;
  metrics: Record<string, DataPointDTO[]>;
}
