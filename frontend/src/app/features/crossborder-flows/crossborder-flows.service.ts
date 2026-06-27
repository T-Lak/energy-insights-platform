import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, Observable, of, EMPTY } from 'rxjs';
import { CrossborderFlowsGlobalPayload } from '../../core/model/payload/crossborder-flows-global.payload';
import { Granularity } from './crossborder-flows.model';

@Injectable()
export class CrossborderFlowsService {
  private granularityPathMap: Record<Granularity, string> = {
    [Granularity.DAILY]: '/daily',
    [Granularity.WEEKLY]: '/weekly',
    [Granularity.MONTHLY]: '/monthly',
  };

  constructor(private httpClient: HttpClient) {}

  getFlowsByGranularity(
    granularity: Granularity,
    regionCode: string = 'DE_LU',
  ): Observable<CrossborderFlowsGlobalPayload> {
    const params = new HttpParams().set('region', regionCode);

    const path: string = '/api/analytics/flows' + this.granularityPathMap[granularity];

    return this.httpClient
      .get<CrossborderFlowsGlobalPayload>(path, {
        params,
      })
      .pipe(
        map((resonse) => resonse),
        catchError((err) => {
          console.error('REST call failed. Waiting for WebSocket fallback.', err);
          return EMPTY;
        }),
      );
  }
}
