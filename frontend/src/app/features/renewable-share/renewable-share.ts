import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { SubpageHeader } from '@shared/components/subpage-header/subpage-header';
import { StackedAreaChart } from '@shared/components/stacked-area-chart/stacked-area-chart';

import { FormsModule } from '@angular/forms';
import { DatePickerModule } from 'primeng/datepicker';
import { Observable, map } from 'rxjs';
import { RenewableShareService } from './renewable-share.service';
import { RenewableMixDTO } from '../../core/model/dto/renewable-mix.dto';

@Component({
  selector: 'app-renewable-share',
  standalone: true,
  imports: [CommonModule, SubpageHeader, StackedAreaChart, FormsModule, DatePickerModule],
  providers: [RenewableShareService],
  templateUrl: './renewable-share.html',
  styleUrl: './renewable-share.scss',
})
export class RenewableShare implements OnInit {
  protected selectedDate: Date | null = new Date();
  protected dailyRenewableMix$: Observable<any> | null = null;

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
          console.log('Fetched daily renewable mix data:', data);
          return data.map((point) => {
            const dbDate = new Date(point.timestamp);

            const formattedTime = dbDate.toLocaleTimeString([], {
              hour: '2-digit',
              minute: '2-digit',
              hour12: false,
            });

            return {
              timestamp: formattedTime,
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
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
