import { SourceRankingPoint } from '../widgets/source-ranking-point.model';

export interface RegionalRankingSnapshot {
  region: string;
  timestamp: string;
  contributions: Record<string, SourceRankingPoint[]>;
}
