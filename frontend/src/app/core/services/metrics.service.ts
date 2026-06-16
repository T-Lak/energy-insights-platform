import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';

import { WebsocketService } from './websocket.service';
import { TimeseriesPointDTO } from '../model/dto/timeseries-point.dto';
import { KpiTimeseriesPayload } from '../model/payload/kpi-timeseries.payload';
import { SourceRankingPointDTO } from '../model/dto/source-ranking-point.dto';
import { SourceRankingPayload } from '../model/payload/source-ranking.payload';

@Injectable({
  providedIn: 'root',
})
export class MetricsService {
  private kpiMetricStream: Record<string, BehaviorSubject<TimeseriesPointDTO[]>> = {};
  private topSourcesStream: Record<string, BehaviorSubject<SourceRankingPointDTO[]>> = {};

  constructor(private websocketService: WebsocketService) {
    this.websocketService.metricsRaw$.subscribe((payload: KpiTimeseriesPayload) => {
      this.routeKpiPayload(payload);
    });

    this.websocketService.sourceRankingRaw$.subscribe((payload: SourceRankingPayload) => {
      this.routeSourceRankingPayload(payload);
    });
  }

  private routeKpiPayload(payload: KpiTimeseriesPayload): void {
    Object.entries(payload.metrics).forEach(([metricName, datapoints]) => {
      if (!this.kpiMetricStream[metricName]) {
        this.kpiMetricStream[metricName] = new BehaviorSubject<TimeseriesPointDTO[]>([]);
      }

      this.kpiMetricStream[metricName].next(datapoints);
    });
  }

  private routeSourceRankingPayload(payload: SourceRankingPayload): void {
    Object.entries(payload.contributions).forEach(([sourceName, rankingPoints]) => {
      if (!this.topSourcesStream[sourceName]) {
        this.topSourcesStream[sourceName] = new BehaviorSubject<SourceRankingPointDTO[]>([]);
      }

      this.topSourcesStream[sourceName].next(rankingPoints);
    });
  }

  getMetricStream(metric: string): Observable<TimeseriesPointDTO[]> {
    if (!this.kpiMetricStream[metric]) {
      this.kpiMetricStream[metric] = new BehaviorSubject<TimeseriesPointDTO[]>([]);
    }

    return this.kpiMetricStream[metric].asObservable();
  }

  getTopSourcesStream(source: string): Observable<SourceRankingPointDTO[]> {
    if (!this.topSourcesStream[source]) {
      this.topSourcesStream[source] = new BehaviorSubject<SourceRankingPointDTO[]>([]);
    }

    return this.topSourcesStream[source].asObservable();
  }
}
