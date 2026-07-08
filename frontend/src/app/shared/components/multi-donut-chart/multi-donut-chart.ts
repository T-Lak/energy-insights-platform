import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { AgCharts } from 'ag-charts-angular';
import { AgChartOptions } from 'ag-charts-community';

import {
  AllCommunityModule as AgGridCommModule,
  ModuleRegistry as AgGridRegistry,
} from 'ag-grid-community';

import { DonutSeriesModule, ModuleRegistry as AgChartsRegistry } from 'ag-charts-community';
import { EnergyCategoryBreakdown } from '../../../features/dashboard/models/energy-category-breakdown.model';
import { SourceContributionPoint } from '../../../features/dashboard/models/source-contribution-point.model';
import { shortenSourceName } from '../../../core/model/domain/sources.model';

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
            renderer: ({ datum }: { datum: EnergyCategoryBreakdown }) => {
              const lines =
                datum.metricPoints
                  ?.map((m: SourceContributionPoint) => {
                    const sourceKey = shortenSourceName(m.source);
                    const formattedLabel = sourceKey
                      .replace(/([A-Z])/g, ' $1')
                      .replace(/^./, (str) => str.toUpperCase());

                    return `
                      <div style="
                        display: flex; 
                        align-items: center;
                        justify-content: space-between; 
                        gap: 24px; 
                        margin-top: 6px; 
                        font-size: 12px;
                        font-family: sans-serif;
                      ">
                        <div style="display: flex; align-items: center; gap: 8px;">
                          <span style="color: #475569;">${formattedLabel}</span>
                        </div>
                        <strong style="color: #1e293b;">${m.percentage.toFixed(1)} MW</strong>
                      </div>`;
                  })
                  .join('') ||
                '<div style="color: #94a3b8; font-size: 12px;">No breakdown available</div>';

              return `
                  <div style="
                    padding: 12px; 
                    background: #ffffff; 
                    border-radius: 8px;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.08);
                    min-width: 230px;
                    font-family: sans-serif;
                  ">
                    <div style="font-size: 13px; font-weight: 600; color: #0f172a; margin-bottom: 4px;">
                      ${datum.timePeriod} — ${datum.category}
                    </div>
                    <div style="font-size: 14px; color: #0f172a; margin-bottom: 8px;">
                      Total: <strong>${datum.amount.toLocaleString()} MWh</strong>
                    </div>
                    <div style="border-top: 1px solid #e2e8f0; margin: 8px 0;"></div>
                    <div style="font-size: 11px; text-transform: uppercase; letter-spacing: 0.05em; color: #838991; font-weight: 600; margin-bottom: 4px;">
                      Source Breakdown
                    </div>
                    ${lines}
                  </div>
                `;
            },
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
            renderer: ({ datum }: { datum: any }) => {
              return `
                <div style="
                  padding: 12px; 
                  background: #ffffff; 
                  border-radius: 8px;
                  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
                  min-width: 230px;
                  font-family: sans-serif;
                ">
                  <div style="
                    font-size: 13px; 
                    font-weight: 600; 
                    color: #0f172a; 
                    margin-bottom: 4px;
                  ">
                    ${datum.label}
                  </div>
                  <div style="font-size: 14px; color: #0f172a;">
                    Total Production: <strong>${datum.amount.toLocaleString()} MWh</strong>
                  </div>
                </div>
              `;
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
