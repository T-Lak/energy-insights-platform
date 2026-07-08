import { FlowGridEdge } from '../../crossborder-flows/models/flow-grid-edge.model';
import { FlowTotalsDTO } from '../../crossborder-flows/models/flow-totals.dto';

export interface RegionalFlowSnapshot {
  hourlyFlowPoints: FlowGridEdge[];
  flowTotals: FlowTotalsDTO[];
  countrySummaries: Object[];
}
