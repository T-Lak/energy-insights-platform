import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, merge, of, catchError } from 'rxjs';
import { filter, shareReplay, switchMap } from 'rxjs/operators';
import { MetricsService } from '../../../core/services/metrics.service';
import { SourceRankingPointDTO } from '../../../core/model/dto/source-ranking-point.dto';
import { TopSourcesCategory } from '../dashboard.model';
import { SourceRankingPayload } from '../../../core/model/payload/source-ranking.payload';

@Injectable()
export class WidgetsService {
  constructor(
    private httpClient: HttpClient,
    private metricsService: MetricsService,
  ) {}

  getWidgetsData(
    regionCode: string = 'DE_LU',
  ): Observable<{ [key in TopSourcesCategory]: Observable<SourceRankingPointDTO[]> }> {
    const params = new HttpParams().set('region', regionCode);

    return this.httpClient
      .get<SourceRankingPayload>('/api/analytics/metrics/top-sources', { params })
      .pipe(
        catchError((error) => {
          console.error('Error fetching top sources data:', error);
          return of({ energy: [], carbon: [] } as SourceRankingPayload);
        }),
        switchMap((latestSnapshot) => {
          const dictionary: { [key in TopSourcesCategory]: Observable<SourceRankingPointDTO[]> } = {
            [TopSourcesCategory.ENERGY_SOURCES]: merge(
              of(latestSnapshot.energy),
              (
                this.metricsService.getTopSourcesStream(
                  TopSourcesCategory.ENERGY_SOURCES,
                ) as Observable<SourceRankingPointDTO[]>
              ).pipe(
                filter(
                  (liveArray): liveArray is SourceRankingPointDTO[] =>
                    Array.isArray(liveArray) && liveArray.length > 0 && liveArray[0] !== undefined,
                ),
              ),
            ),
            [TopSourcesCategory.CARBON_CONTRIBUTORS]: merge(
              of(latestSnapshot.carbon),
              (
                this.metricsService.getTopSourcesStream(
                  TopSourcesCategory.CARBON_CONTRIBUTORS,
                ) as Observable<SourceRankingPointDTO[]>
              ).pipe(
                filter(
                  (liveArray): liveArray is SourceRankingPointDTO[] =>
                    Array.isArray(liveArray) && liveArray.length > 0 && liveArray[0] !== undefined,
                ),
              ),
            ),
          };

          return of(dictionary);
        }),
        shareReplay(1),
      );
  }
}
