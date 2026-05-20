import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';

import { WebsocketService } from './websocket.service';
import { DataPointDTO } from '../models/data-point.dto';
import { MetricUpdateDTO } from '../models/metric-update.dto';

@Injectable({
  providedIn: 'root',
})
export class MetricsService {
  private metricStreams: Record<string, BehaviorSubject<DataPointDTO[]>> = {};

  constructor(private websocketService: WebsocketService) {
    this.websocketService.messages$.subscribe((msg: MetricUpdateDTO) => {
      this.routeMessage(msg);
    });
  }

  private routeMessage(msg: MetricUpdateDTO): void {
    Object.entries(msg.metrics).forEach(([metricName, datapoints]) => {
      if (!this.metricStreams[metricName]) {
        this.metricStreams[metricName] = new BehaviorSubject<DataPointDTO[]>([]);
      }

      this.metricStreams[metricName].next(datapoints);
    });
  }

  getMetricStream(metric: string): Observable<DataPointDTO[]> {
    if (!this.metricStreams[metric]) {
      this.metricStreams[metric] = new BehaviorSubject<DataPointDTO[]>([]);
    }

    return this.metricStreams[metric].asObservable();
  }
}
