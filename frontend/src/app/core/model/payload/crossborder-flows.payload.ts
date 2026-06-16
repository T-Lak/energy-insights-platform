import { FlowPointDTO } from '../dto/flow-point.dto';

export interface CrossborderFlowsPayload {
  region: string;
  generatedAt: string;
  flowPoints: FlowPointDTO[];
}
