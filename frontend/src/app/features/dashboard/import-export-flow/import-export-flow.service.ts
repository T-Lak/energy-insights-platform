import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { filter, Observable } from 'rxjs';

import { CrossborderFlowsService } from '../../../core/services/crossborder-flows.service';
import { CrossborderFlowTotalsTsPayload } from '../../../core/model/payload/crossborder-flow-totals-ts.payload';
import { FlowTotalsDTO } from '../../../core/model/dto/flow-totals.dto';

@Injectable()
export class ImportExportFlowService {
  constructor(
    private httpClient: HttpClient,
    private crossborderFlowService: CrossborderFlowsService,
  ) {}

  getFlowTotalsTimeseries(
    hours: number,
    regionCode: string = 'DE_LU',
  ): Observable<CrossborderFlowTotalsTsPayload> {
    const params = new HttpParams().set('hours', hours).set('region', regionCode);

    return this.httpClient.get<CrossborderFlowTotalsTsPayload>('/api/analytics/flows/timeseries', {
      params,
    });
  }

  getLiveFlowTotals(): Observable<FlowTotalsDTO> {
    return this.crossborderFlowService
      .getFlowTotalsStream()
      .pipe(filter((x): x is NonNullable<typeof x> => x != null));
  }
}
