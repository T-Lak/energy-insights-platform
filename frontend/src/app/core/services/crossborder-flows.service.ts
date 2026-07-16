import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';

import { WebsocketService } from './websocket.service';
import { CrossborderFlowsOverview } from '../../features/crossborder-flows/models/crossborder-flows-overview.model';
import { FlowGridEdge } from '../../features/crossborder-flows/models/flow-grid-edge.model';
import { RegionalFlowTotalsSnapshot } from '../../features/crossborder-flows/models/regional-flow-totals-snapshot.model';
import { FlowTotalsDTO } from '../../features/crossborder-flows/models/flow-totals.dto';
import { MessageService } from 'primeng/api';

@Injectable({
  providedIn: 'root',
})
export class CrossborderFlowsService {
  private flowPointsState = new BehaviorSubject<FlowGridEdge[]>([]);
  private flowTotalsState = new BehaviorSubject<FlowTotalsDTO | null>(null);

  constructor(
    private websocketService: WebsocketService,
    private messageService: MessageService,
  ) {
    this.websocketService.flowPointsRaw$.subscribe((payload: CrossborderFlowsOverview) => {
      this.routeFlowPointsPayload(payload);
    });

    this.websocketService.flowTotalsRaw$.subscribe((payload: RegionalFlowTotalsSnapshot) => {
      this.routeFlowTotalsPayload(payload);

      const totals = payload.flowTotals;

      this.messageService.add({
        severity: 'secondary',
        summary: `Update: ${payload.region}`,
        detail: `Export: ${totals.totalExportMW}MW | Import: ${totals.totalImportMW}MW | Net: ${totals.netExchangeMW}MW`,
        life: 5000,
      });
    });
  }

  private routeFlowPointsPayload(payload: CrossborderFlowsOverview): void {
    this.flowPointsState.next(payload.flowPoints);
  }

  private routeFlowTotalsPayload(payload: RegionalFlowTotalsSnapshot): void {
    this.flowTotalsState.next(payload.flowTotals);
  }

  getFlowPointsStream(): Observable<FlowGridEdge[]> {
    return this.flowPointsState.asObservable();
  }

  getFlowTotalsStream(): Observable<FlowTotalsDTO | null> {
    return this.flowTotalsState.asObservable();
  }
}
