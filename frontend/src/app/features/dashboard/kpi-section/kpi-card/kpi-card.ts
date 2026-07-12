import { Component, Input } from '@angular/core';
import { AsyncPipe, CommonModule } from '@angular/common';

import { Observable } from 'rxjs';
import { startWith, map, shareReplay, pairwise, filter } from 'rxjs/operators';

import { KpiType, KPI_CONFIG } from '../../dashboard.model';
import { Tooltip } from 'primeng/tooltip';
import { TimeseriesPoint } from '../../models/timeseries-point.model';

@Component({
  selector: 'app-kpi-card',
  standalone: true,
  imports: [CommonModule, AsyncPipe, Tooltip],
  templateUrl: './kpi-card.html',
  styleUrl: './kpi-card.scss',
})
export class KpiCard {
  @Input() stream!: Observable<TimeseriesPoint[]>;
  @Input() type!: KpiType;
  @Input() tooltipDescription!: string;

  protected readonly config = KPI_CONFIG;

  currentKpiValue$!: Observable<string>;
  kpiTrend$!: Observable<{ value: string; isUp: boolean; isNeutral: boolean }>;

  constructor() {}

  ngOnInit(): void {
    const kpiConfig = this.config[this.type];

    const data$ = this.stream.pipe(
      filter((data) => !!data && data.length > 0),
      shareReplay(1),
    );

    this.currentKpiValue$ = data$.pipe(
      map((data: TimeseriesPoint[]) => {
        if (!data || data.length === 0 || !data[0]) {
          return kpiConfig.unit === '%' ? '0,00' : '0';
        }

        const latest = data[data.length - 1];

        const formattedValue = new Intl.NumberFormat('de-DE', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2,
        }).format(
          kpiConfig.unit === '%'
            ? latest.value * 100
            : kpiConfig.unit === 'GW'
              ? latest.value / 1000
              : latest.value,
        );

        return formattedValue;
      }),
      shareReplay(1),
    );

    this.kpiTrend$ = data$.pipe(
      startWith([] as TimeseriesPoint[]),
      pairwise(),
      map(([prevArray, currArray]: [TimeseriesPoint[], TimeseriesPoint[]]) => {
        const curr = currArray[currArray.length - 1];

        if (curr.percentageChange != null) {
          return {
            value: `${Math.abs(curr.percentageChange).toFixed(1)}%`,
            isUp: curr.percentageChange > 0,
            isNeutral: false,
          };
        }

        if (prevArray.length === 0) {
          return { value: '0.0%', isUp: false, isNeutral: true };
        }

        const prev = prevArray[prevArray.length - 1];
        const prevVal = prev.value;
        const currVal = curr.value;

        if (prevVal === 0 || prevVal === currVal) {
          return { value: '0.0%', isUp: false, isNeutral: true };
        }

        const change = ((currVal - prevVal) / prevVal) * 100;

        return {
          value: `${Math.abs(change).toFixed(1)}%`,
          isUp: change > 0,
          isNeutral: false,
        };
      }),
    );
  }
}
