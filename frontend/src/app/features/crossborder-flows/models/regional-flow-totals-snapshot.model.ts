import { FlowTotalsDTO } from './flow-totals.dto';

export interface RegionalFlowTotalsSnapshot {
  region: string;
  generatedAt: string;
  flowTotals: FlowTotalsDTO;
}
