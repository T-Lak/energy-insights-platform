import { FlowGridEdge } from './flow-grid-edge.model';

export interface CrossborderFlowsOverview {
  region: string;
  generatedAt: string;
  flowPoints: FlowGridEdge[];
}
