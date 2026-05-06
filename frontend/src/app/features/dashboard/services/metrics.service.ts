import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

import { WebsocketService } from '../../../core/services/websocket.service';
import { MetricMessage } from '../models/metric-message';

@Injectable({
  providedIn: 'root',
})
export class MetricsService {
  private renewableEnergySubject = new BehaviorSubject<number>(0);
  renewableShare$ = this.renewableEnergySubject.asObservable();

  constructor(private websocketService: WebsocketService) {
    this.websocketService.messages$.subscribe((msg: MetricMessage) => {
      this.routeMessage(msg);
    });
  }

  private routeMessage(msg: MetricMessage) {
    switch (msg.metric) {
      case 'renewable_share':
        this.renewableEnergySubject.next(msg.value);
        break;
      // Add more cases here for other metrics as needed
    }
  }
}
