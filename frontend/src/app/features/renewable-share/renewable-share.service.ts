import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map, catchError, EMPTY } from 'rxjs';
import { RenewableMixDTO } from '../../core/model/dto/renewable-mix.dto';

@Injectable()
export class RenewableShareService {
  constructor(private httpClient: HttpClient) {}

  getDailyRenewableShareMix(
    date: string,
    regionCode: string = 'DE_LU',
  ): Observable<RenewableMixDTO[]> {
    const params = new HttpParams().set('date', date).set('region', regionCode);

    const path: string = '/api/analytics/renewables/mix/daily';

    return this.httpClient
      .get<RenewableMixDTO[]>(path, {
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
}
