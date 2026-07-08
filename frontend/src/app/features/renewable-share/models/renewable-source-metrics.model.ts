export interface RenewableSourceMetrics {
  timestamp: string;
  source: string;
  region: string;
  avgGenerationMW: number;
  change1hPercentage: number;
  change24hPercentage: number;
}
