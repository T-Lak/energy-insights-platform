import { SourceRankingPointDTO } from '../dto/source-ranking-point.dto';

export interface SourceRankingPayload {
  region: string;
  timestamp: string;
  contributions: Record<string, SourceRankingPointDTO[]>;
}
