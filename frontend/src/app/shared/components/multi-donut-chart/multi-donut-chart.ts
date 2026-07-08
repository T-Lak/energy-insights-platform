import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { AgCharts } from 'ag-charts-angular';
import { AgChartOptions } from 'ag-charts-community';

import {
  AllCommunityModule as AgGridCommModule,
  ModuleRegistry as AgGridRegistry,
} from 'ag-grid-community';

import { DonutSeriesModule, ModuleRegistry as AgChartsRegistry } from 'ag-charts-community';

AgGridRegistry.registerModules([AgGridCommModule]);

AgChartsRegistry.registerModules([DonutSeriesModule]);

@Component({
  selector: 'app-multi-donut-chart',
  standalone: true,
  imports: [CommonModule, AgCharts],
  templateUrl: './multi-donut-chart.html',
  styleUrl: './multi-donut-chart.scss',
})
export class MultiDonutChart implements OnChanges {
  @Input() dataInput: any[] = [];

  public chartOptions!: AgChartOptions;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['dataInput'] && this.dataInput) {
      this.initChartOptions();
    }
  }

  private initChartOptions(): void {
    const innerRingDataMap = this.dataInput.reduce(
      (acc, current) => {
        const period = current.timePeriod;
        if (!acc[period]) {
          acc[period] = { label: period, amount: 0 };
        }
        acc[period].amount += current.amount;
        return acc;
      },
      {} as Record<string, { label: string; amount: number }>,
    );

    const innerDonutDataset = Object.values(innerRingDataMap);
    const outerDonutDataset = this.dataInput;

    this.chartOptions = {
      background: {
        fill: 'transparent',
      },
      series: [
        {
          type: 'donut',
          data: outerDonutDataset,
          angleKey: 'amount',
          calloutLabelKey: 'category',
          calloutLabel: {
            enabled: false,
          },
          sectorLabelKey: 'category',
          outerRadiusRatio: 1.0,
          innerRadiusRatio: 0.65,
          cornerRadius: 5,
          fillOpacity: 0.85,
          strokeWidth: 1,
          strokes: ['#ffffff', '#ffffff'],
          fills: ['#2F3542', '#535353', '#0f766e'],
          showInLegend: false,
          sectorLabel: { enabled: false },
          tooltip: {
            renderer: ({ datum }: any) => {
              const lines =
                datum.metricPoints
                  ?.map((m: any) => `• ${m.source}: ${m.percentage}%`)
                  .join('<br/>') || '';

              return {
                title: `${datum.timePeriod} — ${datum.category}`,
                content: `<strong>${datum.amount.toLocaleString()} MWh</strong><br/><br/>${lines}`,
                fontSize: 14,
              };
            },
            outerRadiusRatio: 0.7,
            innerRadiusRatio: 0.5,
          },
        },
        {
          type: 'donut',
          data: innerDonutDataset,
          calloutLabelKey: 'label',
          angleKey: 'amount',
          outerRadiusRatio: 0.6,
          innerRadiusRatio: 0.2,
          cornerRadius: 5,
          fillOpacity: 0.85,
          strokeWidth: 1,
          strokes: ['#ffffff', '#ffffff'],
          fills: ['#cacaca', '#707070'],
          title: { showInLegend: true },
          tooltip: {
            renderer: ({ datum }: any) => {
              return {
                title: `${datum.label}`,
                content: `Total Production: <strong>${datum.amount.toLocaleString()} MWh</strong>`,
                fontSize: 14,
              };
            },
          },
        },
      ] as any[],
      legend: {
        enabled: true,
        position: 'top',
        item: {
          label: {
            fontSize: 13.5,
          },
        },
      },
    };
  }
}
