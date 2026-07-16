import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';

import { WebsocketService } from './websocket.service';
import { SourceRankingPoint } from '../../features/dashboard/widgets/source-ranking-point.model';
import { TimeseriesPoint } from '../../features/dashboard/models/timeseries-point.model';
import { RegionalRankingSnapshot } from '../../features/dashboard/models/regional-ranking-snapshot.model';
import { DashboardMetricsTimeline } from '../../features/dashboard/models/dashboard-metrics-timeline.model';
import { MessageService } from 'primeng/api';

@Injectable({
  providedIn: 'root',
})
export class MetricsService {
  private kpiMetricStream: Record<string, BehaviorSubject<TimeseriesPoint[]>> = {};
  private topSourcesStream: Record<string, BehaviorSubject<SourceRankingPoint[]>> = {};

  constructor(
    private websocketService: WebsocketService,
    private messageService: MessageService,
  ) {
    this.websocketService.metricsRaw$.subscribe((payload: DashboardMetricsTimeline) => {
      this.routeKpiPayload(payload);

      const metricsList = Object.keys(payload.metrics).join(', ');

      this.messageService.add({
        severity: 'secondary',
        summary: `Dashboard update: ${payload.region}`,
        detail: `KPI metrics updated: ${metricsList}`,
        life: 5000,
      });
    });

    this.websocketService.sourceRankingRaw$.subscribe((payload: RegionalRankingSnapshot) => {
      this.routeSourceRankingPayload(payload);

      this.messageService.add({
        severity: 'secondary',
        summary: `Top Sources updated for ${payload.region}`,
        detail: 'The energy source rankings have been recalculated.',
        life: 5000,
      });
    });
  }

  private routeKpiPayload(payload: DashboardMetricsTimeline): void {
    Object.entries(payload.metrics).forEach(([metricName, datapoints]) => {
      if (!this.kpiMetricStream[metricName]) {
        this.kpiMetricStream[metricName] = new BehaviorSubject<TimeseriesPoint[]>([]);
      }

      this.kpiMetricStream[metricName].next(datapoints);
    });
  }

  private routeSourceRankingPayload(payload: RegionalRankingSnapshot): void {
    Object.entries(payload.contributions).forEach(([sourceName, rankingPoints]) => {
      if (!this.topSourcesStream[sourceName]) {
        this.topSourcesStream[sourceName] = new BehaviorSubject<SourceRankingPoint[]>([]);
      }

      this.topSourcesStream[sourceName].next(rankingPoints);
    });
  }

  getMetricStream(metric: string): Observable<TimeseriesPoint[]> {
    if (!this.kpiMetricStream[metric]) {
      this.kpiMetricStream[metric] = new BehaviorSubject<TimeseriesPoint[]>([]);
    }

    return this.kpiMetricStream[metric].asObservable();
  }

  getTopSourcesStream(source: string): Observable<SourceRankingPoint[]> {
    if (!this.topSourcesStream[source]) {
      this.topSourcesStream[source] = new BehaviorSubject<SourceRankingPoint[]>([]);
    }

    return this.topSourcesStream[source].asObservable();
  }
}
