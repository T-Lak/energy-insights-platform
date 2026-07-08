import { SourceRankingPoint } from '../widgets/source-ranking-point.model';

export interface DashboardLeaderboardOverview {
  energy: SourceRankingPoint[];
  carbon: SourceRankingPoint[];
}
