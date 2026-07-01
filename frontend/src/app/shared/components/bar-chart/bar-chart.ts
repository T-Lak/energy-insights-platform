import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Observable, Subscription, map } from 'rxjs';

import { AgCharts } from 'ag-charts-angular';

import {
  BarSeriesModule,
  CategoryAxisModule,
  LegendModule,
  ModuleRegistry,
  NumberAxisModule,
} from 'ag-charts-community';
import { BarChartItem } from './bar-chart.model';
import { CommonModule } from '@angular/common';
import { SourceRankingPointDTO } from '../../../core/model/dto/source-ranking-point.dto';
import { getSourceTypeColor, shortenSourceName } from '../../../core/model/domain/sources.model';

ModuleRegistry.registerModules([
  BarSeriesModule,
  CategoryAxisModule,
  LegendModule,
  NumberAxisModule,
]);

@Component({
  selector: 'app-bar-chart',
  standalone: true,
  imports: [CommonModule, AgCharts],
  templateUrl: './bar-chart.html',
  styleUrl: './bar-chart.scss',
})
export class BarChart implements OnInit, OnDestroy {
  @Input() public stream$!: Observable<SourceRankingPointDTO[]>;
  @Input() public type!: 'energy' | 'carbon';
  @Input() public xKey!: string;
  @Input() public yKey!: string;
  @Input() public barWidth!: number;

  private dataSubscription!: Subscription;
  protected chartOptions: any;

  ngOnInit(): void {
    if (this.stream$) {
      this.dataSubscription = this.stream$
        .pipe(
          map((points) =>
            this.type === 'energy' ? this.toEnergyItems(points) : this.toCarbonItems(points),
          ),
        )
        .subscribe({
          next: (mappedData) => {
            console.log(`Received ${this.type} bar chart data:`, mappedData);
            this.setBarOptions(mappedData);
          },
          error: (err) => console.error('Bar chart stream error:', err),
        });
    }
  }

  ngOnDestroy(): void {
    if (this.dataSubscription) {
      this.dataSubscription.unsubscribe();
    }
  }

  private toEnergyItems(points: SourceRankingPointDTO[]): BarChartItem[] {
    const total = points.reduce((sum, p) => sum + p.value, 0);
    return points.map((p) => {
      const pct = total === 0 ? 0 : (p.value / total) * 100;
      return {
        source: shortenSourceName(p.source),
        percentage: parseFloat(pct.toFixed(1)),
        label: `${(p.value / 1000).toFixed(1)} GW`,
        color: getSourceTypeColor(p.source),
      };
    });
  }

  private toCarbonItems(points: SourceRankingPointDTO[]): BarChartItem[] {
    return points.map((p) => {
      const tonnesco2 = p.value / 1_000_000;

      return {
        source: shortenSourceName(p.source),
        intensity: tonnesco2,
        label: `${tonnesco2.toFixed(1)} t CO₂`,
        color: getSourceTypeColor(p.source),
      };
    });
  }

  private setBarOptions(chartData: BarChartItem[]) {
    this.chartOptions = {
      background: { fill: 'transparent' },
      data: [...chartData],
      series: [
        {
          type: 'bar',
          direction: 'horizontal',
          xKey: this.xKey,
          yKey: this.yKey,
          width: this.barWidth,

          label: {
            enabled: false,
          },

          tooltip: {
            renderer: (params: any) => {
              return `<div style="
                      padding: 8px 12px; 
                      font-family: 'Inter', sans-serif; 
                      font-size: 12px; 
                      background-color: #ffffff; 
                      border: 1px solid #e2e8f0; 
                      border-radius: 6px; 
                      box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
                      color: #1e293b;
                      white-space: nowrap;
                    ">
                      <span style="
                        display: inline-block; 
                        width: 8px; 
                        height: 8px; 
                        border-radius: 50%; 
                        background-color: ${params.datum.color || '#a5a5a5'}; 
                        margin-right: 6px;
                      "></span>
                      <span style="text-transform: capitalize;">${params.datum.source}</span>: 
                      <span style="font-weight: 600; color: #475569;">${params.datum.label}</span>
                    </div>`;
            },
          },

          itemStyler: (params: any) => ({
            fill: params.datum.color,
            strokeWidth: 0,
          }),
        },
      ],
      axes: [
        {
          type: 'category',
          position: 'left',
          line: { enabled: false },
          tick: { enabled: false },
          label: {
            fontFamily: 'Inter, sans-serif',
            fontSize: 13,
            fontWeight: '600',
            color: '#495057',
          },
        },
        {
          type: 'number',
          position: 'bottom',
          enabled: true,
          line: { color: '#e2e8f0' },
          tick: { enabled: false },
        },
      ],
      padding: { top: 10, right: 20, bottom: 10, left: 0 },
    };
  }
}
