import { Injectable } from '@angular/core';
import { MetricsService } from '../../../core/services/metrics.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { KpiType } from '../dashboard.model';
import { catchError, filter, merge, of, switchMap } from 'rxjs';
import { DashboardSummarySnapshot } from './dashboard-summary-snapshot.model';

@Injectable()
export class KpiSectionService {
  constructor(
    private httpClient: HttpClient,
    private metricsService: MetricsService,
  ) {}

  getKpiData(regionCode: string = 'DE_LU') {
    const params = new HttpParams().set('region', regionCode);

    return this.httpClient
      .get<DashboardSummarySnapshot>('/api/analytics/metrics/kpi/latest', { params })
      .pipe(
        catchError((error) => {
          console.error('Error fetching KPI data:', error);

          return of({
            renewableShare: { timestamp: new Date(), value: 0.0 },
            carbonIntensity: { timestamp: new Date(), value: 0.0 },
            totalLoad: { timestamp: new Date(), value: 0.0 },
            netBalance: { timestamp: new Date(), value: 0.0 },
          });
        }),
        switchMap((latestData) => {
          return of({
            [KpiType.RENEWABLE_SHARE]: merge(
              of([latestData.renewableShare]),
              this.metricsService
                .getMetricStream(KpiType.RENEWABLE_SHARE)
                .pipe(
                  filter(
                    (liveArray) => liveArray && liveArray.length > 0 && liveArray[0] !== undefined,
                  ),
                ),
            ),

            [KpiType.CARBON_INTENSITY]: merge(
              of([latestData.carbonIntensity]),
              this.metricsService
                .getMetricStream(KpiType.CARBON_INTENSITY)
                .pipe(
                  filter(
                    (liveArray) => liveArray && liveArray.length > 0 && liveArray[0] !== undefined,
                  ),
                ),
            ),

            [KpiType.TOTAL_LOAD]: merge(
              of([latestData.totalLoad]),
              this.metricsService
                .getMetricStream(KpiType.TOTAL_LOAD)
                .pipe(
                  filter(
                    (liveArray) => liveArray && liveArray.length > 0 && liveArray[0] !== undefined,
                  ),
                ),
            ),

            [KpiType.NET_BALANCE]: merge(
              of([latestData.netBalance]),
              this.metricsService
                .getMetricStream(KpiType.NET_BALANCE)
                .pipe(
                  filter(
                    (liveArray) => liveArray && liveArray.length > 0 && liveArray[0] !== undefined,
                  ),
                ),
            ),
          });
        }),
      );
  }
}
