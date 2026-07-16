import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';

import { SelectButtonModule } from 'primeng/selectbutton';

import { SubpageHeader } from '@shared/components/subpage-header/subpage-header';
import { AgTable } from '@shared/components/ag-table/ag-table';
import { FormsModule } from '@angular/forms';
import { ClusteredColumnChart } from '@shared/components/clustered-column-chart/clustered-column-chart';
import { CrossborderFlowsService } from './crossborder-flows.service';
import { Granularity, granularityOptions } from './crossborder-flows.model';
import {
  BehaviorSubject,
  Observable,
  combineLatest,
  map,
  shareReplay,
  startWith,
  switchMap,
} from 'rxjs';
import { ColDef } from 'ag-grid-community';
import { getColumnDefs } from './crossborder-flows.table.columns';
import { ButtonDirective } from 'primeng/button';
import { Tooltip } from 'primeng/tooltip';
import { LineSeries } from '@shared/components/line-series/line-series';

import { SkeletonModule } from 'primeng/skeleton';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { COUNTRY_NAMES, getCountryCodes } from '../../core/model/domain/country.model';

@Component({
  selector: 'app-crossborder-flows',
  standalone: true,
  imports: [
    CommonModule,
    SelectButtonModule,
    FormsModule,
    SubpageHeader,
    AgTable,
    ClusteredColumnChart,
    ButtonDirective,
    Tooltip,
    LineSeries,
    SkeletonModule,
    ProgressSpinnerModule,
  ],
  providers: [CrossborderFlowsService],
  templateUrl: './crossborder-flows.html',
  styleUrl: './crossborder-flows.scss',
})
export class CrossborderFlows implements OnInit {
  private granularity$ = new BehaviorSubject<string>('daily');
  protected selectedTimestamp$ = new BehaviorSubject<string | null>(null);

  public seriesData$!: Observable<any[]>;
  public barChartData$!: Observable<any[]>;
  public tableData$!: Observable<any[]>;
  public activeHourLabel$!: Observable<string>;

  protected granularityOptions = granularityOptions;
  protected selectedGranularity: string = 'daily';

  protected tableColumnDefinitions!: ColDef[];

  protected isLoading$!: Observable<boolean>;

  constructor(private crossborderFlowsService: CrossborderFlowsService) {}

  ngOnInit(): void {
    this.tableColumnDefinitions = getColumnDefs(this.selectedGranularity);

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
      map((state) => {
        const activeData = state.barCountries.filter((b: any) => b.timestamp === state.activeTime);
        const countryCodes = getCountryCodes('DE_LU');

        return countryCodes.map((countryCode) => {
          const found = activeData.find((b: any) => b.toRegion === countryCode);
          return this.toBarChartData(found || null, countryCode);
        });
      }),
    );

    this.tableData$ = sharedState$.pipe(
      map((state) =>
        state.summaries
          .filter((s: any) => s.timestamp === state.activeTime)
          .flatMap((s: any) => this.toTableData(s)),
      ),
    );

    this.activeHourLabel$ = sharedState$.pipe(
      map((state) => (state.activeTime ? this.formatTimeLabel(state.activeTime) : '')),
    );

    this.isLoading$ = combineLatest([this.seriesData$, this.barChartData$, this.tableData$]).pipe(
      map(() => false),
      startWith(true),
    );
  }

  protected onGranularityChange(event: any) {
    if (!event.value) return;

    this.selectedGranularity = event.value;
    this.tableColumnDefinitions = getColumnDefs(event.value);
    this.selectedTimestamp$.next(null);
    this.granularity$.next(event.value);
  }

  protected onLineChartPointClick(clickedTimestamp: string): void {
    if (clickedTimestamp) {
      this.selectedTimestamp$.next(clickedTimestamp);
    }
  }

  protected resetToDefaultState(): void {
    this.selectedTimestamp$.next(null);
  }

  private toLineChartData(dataPoint: any, index: number, allPoints: any[]) {
    const numericTimestamp = new Date(dataPoint.timestamp).getTime();

    return {
      timestamp: dataPoint.timestamp,
      time: numericTimestamp,
      import: Number(dataPoint.totalImportMW.toFixed(2)),
      export: Number(dataPoint.totalExportMW.toFixed(2)),
    };
  }

  private toBarChartData(dataPoint: any | null, countryCode: string) {
    if (!dataPoint) {
      return {
        country: countryCode,
        imports: 0,
        exports: 0,
      };
    }

    return {
      country: dataPoint.toRegion,
      imports: Number(dataPoint.importMW.toFixed(2)),
      exports: Number(dataPoint.exportMW.toFixed(2)),
    };
  }

  private toTableData(dataPoint: any) {
    const baseProperties = {
      timestamp: dataPoint.timestamp,
      country: dataPoint.country,
      netFlow: Number(dataPoint.netFlow.toFixed(2)),
    };

    return [
      {
        ...baseProperties,
        flowDirection: 'Import',
        value: Number(dataPoint.totalImportMW.toFixed(2)),
        shortTermChange: dataPoint.importShortTermChangePercentage,
        longTermChange: dataPoint.importLongTermChangePercentage,
      },
      {
        ...baseProperties,
        flowDirection: 'Export',
        value: Number(dataPoint.totalExportMW.toFixed(2)),
        shortTermChange: dataPoint.exportShortTermChangePercentage,
        longTermChange: dataPoint.exportLongTermChangePercentage,
      },
    ];
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
