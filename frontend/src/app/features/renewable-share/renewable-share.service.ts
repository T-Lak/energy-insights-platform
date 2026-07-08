import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map, catchError, EMPTY } from 'rxjs';
import { RenewableMixPoint } from './models/renewable-mix-point.model';
import { RenewableSourceMetrics } from './models/renewable-source-metrics.model';
import { EnergyCategoryBreakdown } from '../dashboard/models/energy-category-breakdown.model';

@Injectable()
export class RenewableShareService {
  constructor(private httpClient: HttpClient) {}

  getDailyRenewableShareMix(
    date: string,
    regionCode: string = 'DE_LU',
  ): Observable<RenewableMixPoint[]> {
    const params = new HttpParams().set('date', date).set('region', regionCode);

    const path: string = '/api/analytics/renewables/mix/daily';

    return this.httpClient
      .get<RenewableMixPoint[]>(path, {
        params,
      })
      .pipe(
        map((response) => response),
        catchError((err) => {
          console.error('REST call failed.', err);
          return EMPTY;
        }),
      );
  }

  getDailySummaries(
    date: string,
    regionCode: string = 'DE_LU',
  ): Observable<EnergyCategoryBreakdown[]> {
    const params = new HttpParams().set('date', date).set('region', regionCode);

    const path: string = '/api/analytics/renewables/daily-summary';

    return this.httpClient
      .get<EnergyCategoryBreakdown[]>(path, {
        params,
      })
      .pipe(
        map((response) => response),
        catchError((err) => {
          console.error('REST call failed.', err);
          return EMPTY;
        }),
      );
  }

  getDailyMetrics(
    date: string,
    regionCode: string = 'DE_LU',
  ): Observable<RenewableSourceMetrics[]> {
    const params = new HttpParams().set('date', date).set('region', regionCode);

    const path: string = '/api/analytics/renewables/daily-sources';

    return this.httpClient
      .get<RenewableSourceMetrics[]>(path, {
        params,
      })
      .pipe(
        catchError((err) => {
          console.log('REST call failed.', err);
          return EMPTY;
        }),
        map((response) => response),
      );
  }
}
