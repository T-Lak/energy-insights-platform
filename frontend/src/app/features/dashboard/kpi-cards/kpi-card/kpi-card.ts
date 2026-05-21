import { Component, Input } from '@angular/core';
import { AsyncPipe, CommonModule } from '@angular/common';

import { Observable } from 'rxjs';
import { startWith, map } from 'rxjs/operators';

import { MetricsService } from '../../../../core/services/metrics.service';
import { DataPointDTO } from '../../../../core/models/data-point.dto';
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
  @Input() type!: KpiType;
  @Input() tooltipDescription!: string;

  protected readonly config = KPI_CONFIG;

  currentKpiValue$!: Observable<string>;

  constructor(private metricsService: MetricsService) {}

  ngOnInit(): void {
    this.currentKpiValue$ = this.metricsService
      .getMetricStream(this.config[this.type].label.toLowerCase())
      .pipe(
        map((data: DataPointDTO[]) => {
          console.log('data', data);

          if (data.length === 0) {
            return this.config[this.type].unit === '%' ? '0,00' : '0';
          }

          const latest = data[data.length - 1];
          return new Intl.NumberFormat('de-DE', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
          }).format(this.config[this.type].unit === '%' ? latest.value * 100 : latest.value);
        }),
        startWith('0,00'),
      );
  }
}
