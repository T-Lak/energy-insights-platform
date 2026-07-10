import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  AfterViewInit,
  SimpleChanges,
} from '@angular/core';
import { AgCharts } from 'ag-charts-angular';
import { AllCommunityModule, ModuleRegistry } from 'ag-charts-community';
import { FlowDirection, getFlowColor } from '../../../core/model/domain/flows.model';

ModuleRegistry.registerModules([AllCommunityModule]);

@Component({
  selector: 'app-line-series',
  standalone: true,
  imports: [AgCharts],
  template: `<ag-charts
    [options]="chartOptions"
    style="height: 100%; display: block; box-sizing: border-box;"
  ></ag-charts>`,
})
export class LineSeries implements OnChanges, AfterViewInit {
  @Output() dataPointClicked = new EventEmitter<string>();
  @Input() granularity: string = 'daily';
  @Input() seriesData: any[] = [];

  public chartOptions: any = {};
  private isViewInitialized = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['seriesData']) {
      this.seriesData = changes['seriesData'].currentValue;
      if (this.isViewInitialized) {
        this.updateChart();
      }
    }
  }

  ngAfterViewInit(): void {
    this.isViewInitialized = true;
    setTimeout(() => {
      if (this.seriesData?.length) {
        this.updateChart();
      }
    }, 100);
  }

  private updateChart() {
    this.chartOptions = {
      data: this.seriesData,
      background: { visible: false },
      legend: { enabled: false },
      theme: {
        overrides: {
          common: {
            label: {
              color: '#888',
              fontSize: 14,
              fontFamily: 'sans-serif',
            },
            axes: {
              category: { label: { color: '#888' } },
              time: { label: { color: '#888' } },
              number: {
                label: {
                  color: '#888',
                  fontSize: 12,
                  fontFamily: 'sans-serif',
                },
                gridLine: {
                  enabled: false,
                },
              },
            },
          },
        },
      },
      series:
        this.seriesData.length > 0
          ? Object.keys(this.seriesData[0])
              .filter((k) => !['time', 'timestamp', 'displayTime', 'label', 'color'].includes(k))
              .map((key) => {
                const colorHex =
                  key.toLocaleLowerCase() === 'import'
                    ? getFlowColor(FlowDirection.Import)
                    : getFlowColor(FlowDirection.Export);

                return {
                  type: 'line',
                  xKey: 'time',
                  yKey: key,
                  stroke: colorHex,
                  strokeWidth: 2,
                  highlight: {
                    enabled: false,
                    bringToFront: false,
                  },
                  marker: { enabled: true, size: 0, fillOpacity: 0 },
                  nodeClickRange: 'nearest',
                  highlightStyle: { enabled: false },
                  listeners: {
                    nodeClick: (event: any) => {
                      this.dataPointClicked.emit(event.datum['timestamp']);
                    },
                  },
                };
              })
          : [],
      listeners: {
        seriesNodeClick: ({ datum, yKey, seriesId }: any) => {
          this.dataPointClicked.emit(datum['timestamp']);
        },
      },
      axes: {
        x: {
          type: 'time',
          position: 'bottom',
          tick: {
            enabled: false,
          },
          gridLine: {
            enabled: false,
          },
          crosshair: {
            enabled: true,
            label: { enabled: true },
            strokeWidth: 1,
          },
        },
        y: {
          type: 'number',
          position: 'left',
          gridLine: {
            enabled: true,
          },
          label: {
            enabled: true,
            formatter: (params: { value: number }) => {
              if (params.value === undefined || params.value === null) return '';
              return new Intl.NumberFormat('en-US').format(Number(params.value));
            },
          },
          line: { enabled: false },
          crosshair: {
            enabled: true,
          },
        },
      },
    };
  }
}
