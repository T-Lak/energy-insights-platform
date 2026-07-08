import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, merge, of, catchError } from 'rxjs';
import { filter, shareReplay, switchMap } from 'rxjs/operators';
import { MetricsService } from '../../../core/services/metrics.service';
import { SourceRankingPoint } from './source-ranking-point.model';
import { TopSourcesCategory } from '../dashboard.model';
import { DashboardLeaderboardOverview } from '../models/dashboard-leaderboard-overview.model';

@Injectable()
export class WidgetsService {
  constructor(
    private httpClient: HttpClient,
    private metricsService: MetricsService,
  ) {}

  getWidgetsData(
    regionCode: string = 'DE_LU',
  ): Observable<{ [key in TopSourcesCategory]: Observable<SourceRankingPoint[]> }> {
    const params = new HttpParams().set('region', regionCode);

    return this.httpClient
      .get<DashboardLeaderboardOverview>('/api/analytics/metrics/top-sources', { params })
      .pipe(
        catchError((error) => {
          console.error('Error fetching top sources data:', error);
          return of({ energy: [], carbon: [] } as DashboardLeaderboardOverview);
        }),
        switchMap((latestSnapshot) => {
          const dictionary: { [key in TopSourcesCategory]: Observable<SourceRankingPoint[]> } = {
            [TopSourcesCategory.ENERGY_SOURCES]: merge(
              of(latestSnapshot.energy),
              (
                this.metricsService.getTopSourcesStream(
                  TopSourcesCategory.ENERGY_SOURCES,
                ) as Observable<SourceRankingPoint[]>
              ).pipe(
                filter(
                  (liveArray): liveArray is SourceRankingPoint[] =>
                    Array.isArray(liveArray) && liveArray.length > 0 && liveArray[0] !== undefined,
                ),
              ),
            ),
            [TopSourcesCategory.CARBON_CONTRIBUTORS]: merge(
              of(latestSnapshot.carbon),
              (
                this.metricsService.getTopSourcesStream(
                  TopSourcesCategory.CARBON_CONTRIBUTORS,
                ) as Observable<SourceRankingPoint[]>
              ).pipe(
                filter(
                  (liveArray): liveArray is SourceRankingPoint[] =>
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
