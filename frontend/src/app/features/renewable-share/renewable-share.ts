import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { SubpageHeader } from '@shared/components/subpage-header/subpage-header';
import { StackedAreaChart } from '@shared/components/stacked-area-chart/stacked-area-chart';

import { FormsModule } from '@angular/forms';
import { DatePickerModule } from 'primeng/datepicker';
import { Observable, combineLatest, map, shareReplay, startWith } from 'rxjs';
import { RenewableShareService } from './renewable-share.service';
import { RenewableMixPoint } from './models/renewable-mix-point.model';
import { MultiDonutChart } from '@shared/components/multi-donut-chart/multi-donut-chart';
import { SelectModule } from 'primeng/select';
import { getRenewablesDisplayNames } from '../../core/model/domain/sources.model';
import { ColDef } from 'ag-grid-community';
import { getColumnDefs } from './renewable-share.table.columns';
import { AgTable } from '@shared/components/ag-table/ag-table';
import { ButtonDirective } from 'primeng/button';
import { isSameDay } from 'date-fns';

import { SkeletonModule } from 'primeng/skeleton';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

@Component({
  selector: 'app-renewable-share',
  standalone: true,
  imports: [
    CommonModule,
    SubpageHeader,
    StackedAreaChart,
    FormsModule,
    DatePickerModule,
    MultiDonutChart,
    AgTable,
    SelectModule,
    ButtonDirective,
    SkeletonModule,
    ProgressSpinnerModule,
  ],
  providers: [RenewableShareService],
  templateUrl: './renewable-share.html',
  styleUrl: './renewable-share.scss',
})
export class RenewableShare implements OnInit {
  protected selectedDate: Date = new Date();
  protected dailyRenewableMix$: Observable<any> | null = null;
  protected dailySummaries$: Observable<any> | null = null;
  protected sourceComparisons$: Observable<any> | null = null;
  protected tableData$: Observable<any> | null = null;

  protected renewableOptions: string[] = getRenewablesDisplayNames();
  protected selectedSource: string = 'Solar';

  protected tableColumnDefinitions: ColDef[] = getColumnDefs();

  protected maxDate: Date = (() => {
    const d = new Date();
    d.setHours(23, 59, 59, 999);
    return d;
  })();

  protected prevBtnDisabled = false;
  protected nextBtnDisabled = true;
  protected todayBtnDisabled = true;

  protected isLoading$!: Observable<boolean>;

  constructor(private renewableShareService: RenewableShareService) {}

  ngOnInit(): void {
    if (this.selectedDate) {
      this.fetchData(this.selectedDate);
    }
  }

  protected onDateChange(event: Date | Date[] | null) {
    if (event instanceof Date) {
      this.fetchData(event);
    } else if (this.selectedDate) {
      this.fetchData(this.selectedDate);
    }

    this.updateButtons();
  }

  protected onPreviousDayBtnClicked() {
    const newDate = new Date(this.selectedDate);
    newDate.setDate(newDate.getDate() - 1);

    this.selectedDate = newDate;
    this.fetchData(this.selectedDate);

    this.updateButtons();
  }

  protected onNextDayBtnClicked() {
    const newDate = new Date(this.selectedDate);
    newDate.setDate(newDate.getDate() + 1);

    this.selectedDate = newDate;
    this.fetchData(this.selectedDate);

    this.updateButtons();
  }

  protected onTodayBtnClicked() {
    this.selectedDate = new Date();
    this.fetchData(this.selectedDate);

    this.updateButtons();
  }

  private updateButtons() {
    const today = new Date();

    this.todayBtnDisabled = isSameDay(this.selectedDate, today);
    this.nextBtnDisabled = isSameDay(this.selectedDate, this.maxDate);
  }

  private fetchData(date: Date) {
    const formattedDateStr: string = this.formatDate(date);

    this.dailyRenewableMix$ = this.renewableShareService
      .getDailyRenewableShareMix(formattedDateStr)
      .pipe(
        map((data: RenewableMixPoint[]) => {
          return data.map((point) => {
            const epochMs = new Date(point.timestamp).getTime();

            return {
              timestamp: epochMs,
              solar: Number(point.solar.toFixed(2)),
              windOnshore: Number(point.windOnshore.toFixed(2)),
              windOffshore: Number(point.windOffshore.toFixed(2)),
              biomass: Number(point.biomass.toFixed(2)),
              hydro: Number(point.hydro.toFixed(2)),
              geothermal: Number(point.geothermal.toFixed(2)),
              otherRenewable: Number(point.otherRenewable.toFixed(2)),
            };
          });
        }),
        shareReplay(1),
      );

    this.dailySummaries$ = this.renewableShareService.getDailySummaries(formattedDateStr).pipe(
      map((data) => {
        return data;
      }),
    );

    this.tableData$ = this.renewableShareService.getDailyMetrics(formattedDateStr).pipe(
      map((data) => {
        return data;
      }),
    );

    this.isLoading$ = combineLatest([
      this.dailyRenewableMix$,
      this.dailySummaries$,
      this.tableData$,
    ]).pipe(
      map(() => false),
      startWith(true),
    );
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
