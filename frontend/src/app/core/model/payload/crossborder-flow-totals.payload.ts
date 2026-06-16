import { FlowTotalsDTO } from '../dto/flow-totals.dto';

export interface CrossborderFlowTotalsPayload {
  region: string;
  generatedAt: string;
  flowTotals: FlowTotalsDTO;
}
