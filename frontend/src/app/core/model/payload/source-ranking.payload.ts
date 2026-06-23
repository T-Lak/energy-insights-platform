import { SourceRankingPointDTO } from '../dto/source-ranking-point.dto';

export interface SourceRankingPayload {
  energy: SourceRankingPointDTO[];
  carbon: SourceRankingPointDTO[];
}
