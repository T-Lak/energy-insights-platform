import { Component, Input } from '@angular/core';
import { AsyncPipe, CommonModule } from '@angular/common';

import { Observable } from 'rxjs';
import { startWith, map, shareReplay, pairwise } from 'rxjs/operators';

import { TimeseriesPointDTO } from '../../../../core/model/dto/timeseries-point.dto';
import { KpiType, KPI_CONFIG } from '../../dashboard.model';
import { Tooltip } from 'primeng/tooltip';

@Component({
  selector: 'app-kpi-card',
  standalone: true,
  imports: [CommonModule, AsyncPipe, Tooltip],
  templateUrl: './kpi-card.html',
  styleUrl: './kpi-card.scss',
})
export class KpiCard {
  @Input() stream!: Observable<TimeseriesPointDTO[]>;
  @Input() type!: KpiType;
  @Input() tooltipDescription!: string;

  protected readonly config = KPI_CONFIG;

  currentKpiValue$!: Observable<string>;
  kpiTrend$!: Observable<{ value: string; isUp: boolean; isNeutral: boolean }>;

  constructor() {}

  ngOnInit(): void {
    const kpiConfig = this.config[this.type];

    this.currentKpiValue$ = this.stream.pipe(
      map((data: TimeseriesPointDTO[]) => {
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

    this.kpiTrend$ = this.currentKpiValue$.pipe(
      map((valueStr) => {
        if (!valueStr) return 0;
        const cleanStr = valueStr
          .replace(/\./g, '')
          .replace(',', '.')
          .replace(/[^0-9.]/g, '');
        return parseFloat(cleanStr) || 0;
      }),
      startWith(0),
      pairwise(),
      map(([prev, current]) => {
        if (prev === 0 || prev === current) {
          return { value: '0.0%', isUp: false, isNeutral: true };
        }

        const change = ((current - prev) / prev) * 100;

        return {
          value: `${Math.abs(change).toFixed(1)}%`,
          isUp: change > 0,
          isNeutral: false,
        };
      }),
    );
  }
}
