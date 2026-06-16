import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';

import { WebsocketService } from './websocket.service';
import { CrossborderFlowsPayload } from '../model/payload/crossborder-flows.payload';
import { FlowPointDTO } from '../model/dto/flow-point.dto';
import { CrossborderFlowTotalsPayload } from '../model/payload/crossborder-flow-totals.payload';
import { FlowTotalsDTO } from '../model/dto/flow-totals.dto';

@Injectable({
  providedIn: 'root',
})
export class CrossborderFlowsService {
  private flowPointsState = new BehaviorSubject<FlowPointDTO[]>([]);
  private flowTotalsState = new BehaviorSubject<FlowTotalsDTO | null>(null);

  constructor(private websocketService: WebsocketService) {
    this.websocketService.flowPointsRaw$.subscribe((payload: CrossborderFlowsPayload) => {
      this.routeFlowPointsPayload(payload);
    });

    this.websocketService.flowTotalsRaw$.subscribe((payload: CrossborderFlowTotalsPayload) => {
      this.routeFlowTotalsPayload(payload);
    });
  }

  private routeFlowPointsPayload(payload: CrossborderFlowsPayload): void {
    this.flowPointsState.next(payload.flowPoints);
  }

  private routeFlowTotalsPayload(payload: CrossborderFlowTotalsPayload): void {
    this.flowTotalsState.next(payload.flowTotals);
  }

  getFlowPointsStream(): Observable<FlowPointDTO[]> {
    return this.flowPointsState.asObservable();
  }

  getFlowTotalsStream(): Observable<FlowTotalsDTO | null> {
    return this.flowTotalsState.asObservable();
  }
}
