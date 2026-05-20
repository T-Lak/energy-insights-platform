import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';

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
export class BarChart implements OnInit, OnChanges {
  @Input() public data!: BarChartItem[];
  @Input() public xKey!: string;
  @Input() public yKey!: string;
  @Input() public barWidth!: number;

  protected chartOptions: any;

  ngOnInit(): void {
    if (this.data && this.data.length > 0) {
      this.setBarOptions();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data'] && this.data && this.data.length > 0) {
      this.setBarOptions();
    }
  }

  private setBarOptions() {
    this.chartOptions = {
      background: { fill: 'transparent' },
      data: this.data,
      series: [
        {
          type: 'bar',
          direction: 'horizontal',
          xKey: this.xKey,
          yKey: this.yKey,
          width: this.barWidth,
          label: { enabled: false },
          itemStyler: (params: any) => {
            return {
              fill: params.datum.color,
              strokeWidth: 0,
            };
          },
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
        { type: 'number', position: 'bottom' },
      ],
      padding: { top: 0, right: 0, bottom: 0, left: 0 },
    };
  }
}
