import { SourceContributionPoint } from './source-contribution-point.model';

export interface EnergyCategoryBreakdown {
  timePeriod: string;
  category: string;
  amount: number;
  metricPoints: SourceContributionPoint[];
}
