import { FlowTotalsDTO } from './flow-totals.dto';

export interface RegionFlowTotalsTimeline {
  region: string;
  generatedAt: string;
  flowTotals: FlowTotalsDTO[];
}
