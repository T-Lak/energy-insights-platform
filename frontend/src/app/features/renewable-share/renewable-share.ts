import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { SubpageHeader } from '@shared/components/subpage-header/subpage-header';
import { StackedAreaChart } from '@shared/components/stacked-area-chart/stacked-area-chart';

@Component({
  selector: 'app-renewable-share',
  standalone: true,
  imports: [CommonModule, SubpageHeader, StackedAreaChart],
  templateUrl: './renewable-share.html',
  styleUrl: './renewable-share.scss',
})
export class RenewableShare {
  protected data = [
    { time: '00:00', solar: 0, windOnshore: 420, windOffshore: 210, biomass: 80, hydro: 45 },
    { time: '04:00', solar: 0, windOnshore: 440, windOffshore: 230, biomass: 80, hydro: 45 },
    { time: '08:00', solar: 150, windOnshore: 390, windOffshore: 200, biomass: 85, hydro: 50 },
    { time: '12:00', solar: 680, windOnshore: 310, windOffshore: 180, biomass: 85, hydro: 55 },
    { time: '16:00', solar: 410, windOnshore: 350, windOffshore: 190, biomass: 85, hydro: 50 },
    { time: '20:00', solar: 10, windOnshore: 460, windOffshore: 240, biomass: 80, hydro: 45 },
  ];
}
