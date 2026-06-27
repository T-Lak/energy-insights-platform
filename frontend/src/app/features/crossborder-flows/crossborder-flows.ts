import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';

import { SelectButtonModule } from 'primeng/selectbutton';

import { SubpageHeader } from '@shared/components/subpage-header/subpage-header';
import { AgTable } from '@shared/components/ag-table/ag-table';
import { LineChart } from '@shared/components/line-chart/line-chart';
import { FormsModule } from '@angular/forms';
import { ClusteredColumnChart } from '@shared/components/clustered-column-chart/clustered-column-chart';
import { CrossborderFlowsService } from './crossborder-flows.service';
import { Granularity, granularityOptions } from './crossborder-flows.model';
import { BehaviorSubject, Observable, combineLatest, map, shareReplay, switchMap } from 'rxjs';

@Component({
  selector: 'app-crossborder-flows',
  standalone: true,
  imports: [
    CommonModule,
    SelectButtonModule,
    FormsModule,
    SubpageHeader,
    AgTable,
    LineChart,
    ClusteredColumnChart,
  ],
  providers: [CrossborderFlowsService],
  templateUrl: './crossborder-flows.html',
  styleUrl: './crossborder-flows.scss',
})
export class CrossborderFlows implements OnInit {
  private granularity$ = new BehaviorSubject<string>('daily');
  private selectedTimestamp$ = new BehaviorSubject<string | null>(null);

  public seriesData$!: Observable<any[]>;
  public barChartData$!: Observable<any[]>;
  public tableData$!: Observable<any[]>;
  public activeHourLabel$!: Observable<string>;

  protected granularityOptions = granularityOptions;
  public selectedGranularity: string = 'daily';

  constructor(private crossborderFlowsService: CrossborderFlowsService) {}

  ngOnInit(): void {
    const flowsPayload$ = this.granularity$.pipe(
      switchMap((granularity) => {
        const enumMap: Record<string, Granularity> = {
          weekly: Granularity.WEEKLY,
          monthly: Granularity.MONTHLY,
          daily: Granularity.DAILY,
        };
        return this.crossborderFlowsService.getFlowsByGranularity(enumMap[granularity]);
      }),
      shareReplay(1),
    );

    this.seriesData$ = flowsPayload$.pipe(
      map((payload) => {
        const totals = payload.flowTotals || [];
        return totals.map((t: any, idx: number) => this.toLineChartData(t, idx, totals));
      }),
    );

    const sharedState$ = combineLatest({
      payload: flowsPayload$,
      selectedTime: this.selectedTimestamp$,
    }).pipe(
      map(({ payload, selectedTime }) => {
        const lineTotals = payload.flowTotals || [];
        const barCountries = payload.hourlyFlowPoints || [];
        const summaries = payload.countrySummaries || [];
        const activeTime = selectedTime ?? lineTotals.at(-1)?.timestamp ?? null;

        return { barCountries, summaries, activeTime };
      }),
      shareReplay(1),
    );

    this.barChartData$ = sharedState$.pipe(
      map((state) =>
        state.barCountries
          .filter((b: any) => b.timestamp === state.activeTime)
          .map((b: any) => this.toBarChartData(b)),
      ),
    );

    this.tableData$ = sharedState$.pipe(
      map((state) => state.summaries.map((s: any) => this.toTableData(s))),
    );

    this.activeHourLabel$ = sharedState$.pipe(
      map((state) => (state.activeTime ? this.formatTimeLabel(state.activeTime) : '')),
    );
  }

  public onGranularityChange(event: any) {
    if (!event.value) return;

    this.selectedGranularity = event.value;
    this.selectedTimestamp$.next(null);
    this.granularity$.next(event.value);
  }

  public onLineChartPointClick(clickedDataPoint: any): void {
    if (clickedDataPoint?.timestamp) {
      this.selectedTimestamp$.next(clickedDataPoint.timestamp);
    }
  }

  private toLineChartData(dataPoint: any, index: number, allPoints: any[]) {
    const date = new Date(dataPoint.timestamp);
    let cleanTime;
    let uniqueId;

    if (this.selectedGranularity === 'monthly') {
      cleanTime = date.toLocaleDateString([], {
        day: '2-digit',
        month: 'short',
      });

      uniqueId = `${cleanTime} (${date.getFullYear()})`;
    } else if (this.selectedGranularity === 'weekly') {
      const currentDay = date.toDateString();
      const isFirstHourOfDay =
        index === 0 || new Date(allPoints[index - 1].timestamp).toDateString() !== currentDay;

      if (isFirstHourOfDay) {
        cleanTime = date.toLocaleDateString([], { weekday: 'short' });
      } else {
        cleanTime = '';
      }

      const fullTimeStr = date.toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
      });
      uniqueId = `${fullTimeStr} (${date.toLocaleDateString([], { day: '2-digit', month: '2-digit' })})`;
    } else {
      cleanTime = date.toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
      });

      uniqueId = `${cleanTime} (${date.toLocaleDateString([], { day: '2-digit', month: '2-digit' })})`;
    }

    return {
      timestamp: dataPoint.timestamp,
      time: uniqueId,
      displayTime: cleanTime,
      importValue: Number(dataPoint.totalImportMW.toFixed(2)),
      exportValue: Number(dataPoint.totalExportMW.toFixed(2)),
    };
  }

  private toBarChartData(dataPoint: any) {
    return {
      country: dataPoint.toRegion,
      imports: Number(dataPoint.importMW.toFixed(2)),
      exports: Number(dataPoint.exportMW.toFixed(2)),
    };
  }

  private toTableData(dataPoint: any) {
    return {
      country: dataPoint.toRegion,
      importValue: Number(dataPoint.totalImportMW.toFixed(2)),
      exportValue: Number(dataPoint.totalExportMW.toFixed(2)),
      netFlow: Number(dataPoint.netFlow.toFixed(2)),
      importChangeShort: dataPoint.importShortTermPercentage,
      exportChangeShort: dataPoint.exportShortTermPercentage,
    };
  }

  private formatTimeLabel(isoString: string): string {
    const date = new Date(isoString);

    const timeStr = date.toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });

    const dateStr = date.toLocaleDateString([], { day: '2-digit', month: '2-digit' });

    return `${timeStr} (${dateStr})`;
  }
}
