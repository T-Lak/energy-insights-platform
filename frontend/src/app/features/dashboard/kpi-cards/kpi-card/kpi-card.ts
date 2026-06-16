import { Component, Input } from '@angular/core';
import { AsyncPipe, CommonModule } from '@angular/common';

import { Observable } from 'rxjs';
import { startWith, map, shareReplay } from 'rxjs/operators';

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

  constructor() {}

  ngOnInit(): void {
    const kpiConfig = this.config[this.type];

    this.currentKpiValue$ = this.stream.pipe(
      map((data: TimeseriesPointDTO[]) => {
        if (!data || data.length === 0 || !data[0]) {
          return kpiConfig.unit === '%' ? '0,00' : '0';
        }

        const latest = data[data.length - 1];

        return new Intl.NumberFormat('de-DE', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2,
        }).format(
          kpiConfig.unit === '%'
            ? latest.value * 100
            : kpiConfig.unit === 'GW'
              ? latest.value / 1000
              : latest.value,
        );
      }),
      shareReplay(1),
    );
  }
}
