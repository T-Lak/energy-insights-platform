import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Subject } from 'rxjs';
import { CrossborderFlowsOverview } from '../../features/crossborder-flows/models/crossborder-flows-overview.model';
import { RegionalFlowTotalsSnapshot } from '../../features/crossborder-flows/models/regional-flow-totals-snapshot.model';
import { RegionalRankingSnapshot } from '../../features/dashboard/models/regional-ranking-snapshot.model';
import { DashboardMetricsTimeline } from '../../features/dashboard/models/dashboard-metrics-timeline.model';

@Injectable({
  providedIn: 'root',
})
export class WebsocketService {
  private stompClient!: Client;

  private kpiMetricsSubject = new Subject<DashboardMetricsTimeline>();
  private sourceRankingSubject = new Subject<RegionalRankingSnapshot>();
  private flowPointsSubject = new Subject<CrossborderFlowsOverview>();
  private flowTotalsSubject = new Subject<RegionalFlowTotalsSnapshot>();

  private lastUpdateSubject = new BehaviorSubject<Date>(new Date());

  metricsRaw$ = this.kpiMetricsSubject.asObservable();
  sourceRankingRaw$ = this.sourceRankingSubject.asObservable();
  flowPointsRaw$ = this.flowPointsSubject.asObservable();
  flowTotalsRaw$ = this.flowTotalsSubject.asObservable();

  lastUpdate$ = this.lastUpdateSubject.asObservable();

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
          this.lastUpdateSubject.next(new Date());
        });

        this.stompClient.subscribe('/topic/grid_top_sources', (message: IMessage) => {
          const body = JSON.parse(message.body);

          this.sourceRankingSubject.next(body);
        });

        this.stompClient.subscribe('/topic/flow_points', (message: IMessage) => {
          const body = JSON.parse(message.body);

          this.flowPointsSubject.next(body);
          this.lastUpdateSubject.next(new Date());
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
