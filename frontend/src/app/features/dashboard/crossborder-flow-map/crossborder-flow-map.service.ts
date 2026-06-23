import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CrossborderFlowsService } from '../../../core/services/crossborder-flows.service';
import { catchError, filter, map, merge, Observable, of } from 'rxjs';
import { FlowPointDTO } from '../../../core/model/dto/flow-point.dto';
import { LatestFlowsPayload } from '../../../core/model/payload/latest-flow-points.payload';

@Injectable()
export class CrossborderFlowMapService {
  constructor(
    private httpClient: HttpClient,
    private crossborderFlowService: CrossborderFlowsService,
  ) {}

  getFlowPoints(regionCode: string = 'DE_LU') {
    const params = new HttpParams().set('region', regionCode);
    const initialFlowPoints$: Observable<FlowPointDTO[]> = this.httpClient
      .get<LatestFlowsPayload>('/api/analytics/metrics/flows/latest', {
        params,
      })
      .pipe(
        map((response) => response.flowPoints),
        catchError((err) => {
          console.error('REST call failed. Waiting for WebSocket fallback', err);
          return of([]);
        }),
      );

    const liveFlowPoints$: Observable<FlowPointDTO[]> = this.crossborderFlowService
      .getFlowPointsStream()
      .pipe(
        filter((x): x is NonNullable<typeof x> => x != null),
        catchError((err) => {
          console.error('WebSocket dropped or unreachable.', err);
          return of([]);
        }),
      );

    return merge(initialFlowPoints$, liveFlowPoints$);
  }
}
