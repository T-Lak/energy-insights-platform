import { FlowTotalsDTO } from '../dto/flow-totals.dto';

export interface CrossborderFlowTotalsTsPayload {
  region: string;
  generatedAt: string;
  flowTotals: FlowTotalsDTO[];
}
