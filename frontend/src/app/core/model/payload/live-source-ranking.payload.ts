import { SourceRankingPointDTO } from '../dto/source-ranking-point.dto';

export interface LiveSourceRankingPayload {
  region: string;
  timestamp: string;
  contributions: Record<string, SourceRankingPointDTO[]>;
}
