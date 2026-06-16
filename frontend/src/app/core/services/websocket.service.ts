import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';
import { KpiTimeseriesPayload } from '../model/payload/kpi-timeseries.payload';
import { CrossborderFlowsPayload } from '../model/payload/crossborder-flows.payload';
import { CrossborderFlowTotalsPayload } from '../model/payload/crossborder-flow-totals.payload';
import { SourceRankingPayload } from '../model/payload/source-ranking.payload';

@Injectable({
  providedIn: 'root',
})
export class WebsocketService {
  private stompClient!: Client;

  private kpiMetricsSubject = new Subject<KpiTimeseriesPayload>();
  private sourceRankingSubject = new Subject<SourceRankingPayload>();
  private flowPointsSubject = new Subject<CrossborderFlowsPayload>();
  private flowTotalsSubject = new Subject<CrossborderFlowTotalsPayload>();

  metricsRaw$ = this.kpiMetricsSubject.asObservable();
  sourceRankingRaw$ = this.sourceRankingSubject.asObservable();
  flowPointsRaw$ = this.flowPointsSubject.asObservable();
  flowTotalsRaw$ = this.flowTotalsSubject.asObservable();

  constructor() {
    this.connect();
  }

  connect(): void {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-analytics'),

      reconnectDelay: 5000,

      onConnect: () => {
        console.log('Connected to websocket');

        this.stompClient.subscribe('/topic/grid_metrics', (message: IMessage) => {
          const body = JSON.parse(message.body);

          this.kpiMetricsSubject.next(body);
        });

        this.stompClient.subscribe('/topic/grid_sources', (message: IMessage) => {
          const body = JSON.parse(message.body);

          this.sourceRankingSubject.next(body);
        });

        this.stompClient.subscribe('/topic/flow_points', (message: IMessage) => {
          const body = JSON.parse(message.body);

          this.flowPointsSubject.next(body);
        });

        this.stompClient.subscribe('/topic/flow_totals', (message: IMessage) => {
          const body = JSON.parse(message.body);

          this.flowTotalsSubject.next(body);
        });
      },

      onStompError: (frame) => {
        console.error('Broker error:', frame);
      },
    });

    this.stompClient.activate();
  }
}
