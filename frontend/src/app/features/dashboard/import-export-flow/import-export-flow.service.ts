import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { filter, Observable } from 'rxjs';

import { CrossborderFlowsService } from '../../../core/services/crossborder-flows.service';
import { RegionFlowTotalsTimeline } from '../../crossborder-flows/models/regional-flow-totals-timeline.model';
import { FlowTotalsDTO } from '../../crossborder-flows/models/flow-totals.dto';

@Injectable()
export class ImportExportFlowService {
  constructor(
    private httpClient: HttpClient,
    private crossborderFlowService: CrossborderFlowsService,
  ) {}

  getFlowTotalsTimeseries(
    hours: number,
    regionCode: string = 'DE_LU',
  ): Observable<RegionFlowTotalsTimeline> {
    const params = new HttpParams().set('hours', hours).set('region', regionCode);

    return this.httpClient.get<RegionFlowTotalsTimeline>('/api/analytics/flows/timeseries', {
      params,
    });
  }

  getLiveFlowTotals(): Observable<FlowTotalsDTO> {
    return this.crossborderFlowService
      .getFlowTotalsStream()
      .pipe(filter((x): x is NonNullable<typeof x> => x != null));
  }
}
