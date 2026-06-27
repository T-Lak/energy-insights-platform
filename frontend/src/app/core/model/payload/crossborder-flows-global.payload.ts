import { FlowPointDTO } from '../dto/flow-point.dto';
import { FlowTotalsDTO } from '../dto/flow-totals.dto';

export interface CrossborderFlowsGlobalPayload {
  hourlyFlowPoints: FlowPointDTO[];
  flowTotals: FlowTotalsDTO[];
  countrySummaries: Object[];
}
