import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { SubpageHeader } from '@shared/components/subpage-header/subpage-header';
import { StackedAreaChart } from '@shared/components/stacked-area-chart/stacked-area-chart';

import { FormsModule } from '@angular/forms';
import { DatePickerModule } from 'primeng/datepicker';
import { Observable, map } from 'rxjs';
import { RenewableShareService } from './renewable-share.service';
import { RenewableMixDTO } from '../../core/model/dto/renewable-mix.dto';
import { MultiDonutChart } from '@shared/components/multi-donut-chart/multi-donut-chart';
import { LineChart } from '@shared/components/line-chart/line-chart';
import { SelectModule } from 'primeng/select';
import { getRenewablesDisplayNames } from '../../core/model/domain/sources.model';

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
    LineChart,
    SelectModule,
  ],
  providers: [RenewableShareService],
  templateUrl: './renewable-share.html',
  styleUrl: './renewable-share.scss',
})
export class RenewableShare implements OnInit {
  protected selectedDate: Date | null = new Date();
  protected dailyRenewableMix$: Observable<any> | null = null;
  protected dailySummaries$: Observable<any> | null = null;

  protected renewableOptions: string[] = getRenewablesDisplayNames();
  protected selectedSource: string = 'Solar';

  protected sourceComparisonOptions: string[] = ['Yesterday', 'Last Week', 'Last Month'];
  protected selectedSourceComparison: string = 'Yesterday';

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
  }

  private fetchData(date: Date) {
    const formattedDateStr: string = this.formatDate(date);
    this.dailyRenewableMix$ = this.renewableShareService
      .getDailyRenewableShareMix(formattedDateStr)
      .pipe(
        map((data: RenewableMixDTO[]) => {
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
      );

    this.dailySummaries$ = this.renewableShareService.getDailySummaries(formattedDateStr).pipe(
      map((data) => {
        return data;
      }),
    );
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
