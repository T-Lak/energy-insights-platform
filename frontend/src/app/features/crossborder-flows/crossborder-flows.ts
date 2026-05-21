import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

import { SelectButtonModule } from 'primeng/selectbutton';

import { SubpageHeader } from '@shared/components/subpage-header/subpage-header';
import { AgTable } from '@shared/components/ag-table/ag-table';
import { LineChart } from '@shared/components/line-chart/line-chart';
import { FormsModule } from '@angular/forms';
import { ClusteredColumnChart } from '@shared/components/clustered-column-chart/clustered-column-chart';

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
  templateUrl: './crossborder-flows.html',
  styleUrl: './crossborder-flows.scss',
})
export class CrossborderFlows {
  protected granularityOptions = [
    { label: 'Daily', value: 'day' },
    { label: 'Monthly', value: 'month' },
    { label: 'Yearly', value: 'year' },
  ];

  public selectedGranularity: string = 'day';

  // 3. Trigger tracking when the selection updates
  public onGranularityChange(event: any) {
    console.log('Switched to:', this.selectedGranularity);
    // Execute data loading/transformation pipeline here...
  }

  protected data = [
    { time: '00:00', importValue: 400, exportValue: 550 },
    { time: '00:15', importValue: 390, exportValue: 560 },
    { time: '00:30', importValue: 410, exportValue: 540 },
    { time: '00:45', importValue: 400, exportValue: 550 },
    { time: '01:00', importValue: 380, exportValue: 600 },
    { time: '01:15', importValue: 370, exportValue: 610 },
    { time: '01:30', importValue: 390, exportValue: 590 },
    { time: '01:45', importValue: 380, exportValue: 600 },
    { time: '02:00', importValue: 350, exportValue: 650 },
    { time: '02:15', importValue: 370, exportValue: 620 },
    { time: '02:30', importValue: 380, exportValue: 600 },
    { time: '02:45', importValue: 400, exportValue: 580 },
    { time: '03:00', importValue: 450, exportValue: 550 },
    { time: '03:15', importValue: 480, exportValue: 530 },
    { time: '03:30', importValue: 520, exportValue: 510 },
    { time: '03:45', importValue: 560, exportValue: 500 },
    { time: '04:00', importValue: 600, exportValue: 500 },
    { time: '04:15', importValue: 630, exportValue: 490 },
    { time: '04:30', importValue: 660, exportValue: 480 },
    { time: '04:45', importValue: 680, exportValue: 470 },
    { time: '05:00', importValue: 750, exportValue: 470 },
    { time: '05:15', importValue: 780, exportValue: 460 },
    { time: '05:30', importValue: 820, exportValue: 450 },
    { time: '05:45', importValue: 860, exportValue: 450 },
    { time: '06:00', importValue: 900, exportValue: 450 },
    { time: '06:15', importValue: 880, exportValue: 550 },
    { time: '06:30', importValue: 860, exportValue: 680 },
    { time: '06:45', importValue: 850, exportValue: 800 },
    { time: '07:00', importValue: 850, exportValue: 925 },
    { time: '07:15', importValue: 830, exportValue: 1050 },
    { time: '07:30', importValue: 820, exportValue: 1200 },
    { time: '07:45', importValue: 800, exportValue: 1350 },
    { time: '08:00', importValue: 800, exportValue: 1400 },
    { time: '08:15', importValue: 780, exportValue: 1500 },
    { time: '08:30', importValue: 750, exportValue: 1620 },
    { time: '08:45', importValue: 730, exportValue: 1700 },
    { time: '09:00', importValue: 720, exportValue: 1745 },
    { time: '09:15', importValue: 850, exportValue: 1450 },
    { time: '09:30', importValue: 980, exportValue: 1150 },
    { time: '09:45', importValue: 1100, exportValue: 850 },
    { time: '10:00', importValue: 1200, exportValue: 600 }, // Start of your snippet
    { time: '10:15', importValue: 1140, exportValue: 620 },
    { time: '10:30', importValue: 1060, exportValue: 680 },
    { time: '10:45', importValue: 970, exportValue: 710 },
    { time: '11:00', importValue: 900, exportValue: 750 },
    { time: '11:15', importValue: 980, exportValue: 800 },
    { time: '11:30', importValue: 1100, exportValue: 840 },
    { time: '11:45', importValue: 1260, exportValue: 890 },
    { time: '12:00', importValue: 1400, exportValue: 950 },
    { time: '12:15', importValue: 1340, exportValue: 910 },
    { time: '12:30', importValue: 1250, exportValue: 860 },
    { time: '12:45', importValue: 1170, exportValue: 800 },
    { time: '13:00', importValue: 1100, exportValue: 720 },
    { time: '13:15', importValue: 1130, exportValue: 760 },
    { time: '13:30', importValue: 1160, exportValue: 810 },
    { time: '13:45', importValue: 1190, exportValue: 880 },
    { time: '14:00', importValue: 1200, exportValue: 930 },
    { time: '14:15', importValue: 1140, exportValue: 980 },
    { time: '14:30', importValue: 1060, exportValue: 1050 },
    { time: '14:45', importValue: 970, exportValue: 1120 },
    { time: '15:00', importValue: 900, exportValue: 1200 },
    { time: '15:15', importValue: 980, exportValue: 1140 },
    { time: '15:30', importValue: 1100, exportValue: 1060 },
    { time: '15:45', importValue: 1260, exportValue: 950 },
    { time: '16:00', importValue: 1400, exportValue: 900 },
    { time: '16:15', importValue: 1340, exportValue: 840 },
    { time: '16:30', importValue: 1250, exportValue: 790 },
    { time: '16:45', importValue: 1170, exportValue: 720 },
    { time: '17:00', importValue: 1100, exportValue: 650 }, // End of your snippet
    { time: '17:15', importValue: 1200, exportValue: 650 },
    { time: '17:30', importValue: 1300, exportValue: 650 },
    { time: '17:45', importValue: 1400, exportValue: 650 },
    { time: '18:00', importValue: 1500, exportValue: 650 },
    { time: '18:15', importValue: 1550, exportValue: 640 },
    { time: '18:30', importValue: 1600, exportValue: 630 },
    { time: '18:45', importValue: 1620, exportValue: 630 },
    { time: '19:00', importValue: 1650, exportValue: 625 },
    { time: '19:15', importValue: 1700, exportValue: 615 },
    { time: '19:30', importValue: 1750, exportValue: 610 },
    { time: '19:45', importValue: 1780, exportValue: 600 },
    { time: '20:00', importValue: 1800, exportValue: 600 },
    { time: '20:15', importValue: 1700, exportValue: 610 },
    { time: '20:30', importValue: 1600, exportValue: 620 },
    { time: '20:45', importValue: 1500, exportValue: 630 },
    { time: '21:00', importValue: 1400, exportValue: 650 },
    { time: '21:15', importValue: 1280, exportValue: 650 },
    { time: '21:30', importValue: 1150, exportValue: 650 },
    { time: '21:45', importValue: 1050, exportValue: 650 },
    { time: '22:00', importValue: 950, exportValue: 650 },
    { time: '22:15', importValue: 880, exportValue: 650 },
    { time: '22:30', importValue: 820, exportValue: 650 },
    { time: '22:45', importValue: 750, exportValue: 650 },
    { time: '23:00', importValue: 700, exportValue: 650 },
    { time: '23:15', importValue: 650, exportValue: 620 },
    { time: '23:30', importValue: 580, exportValue: 590 },
    { time: '23:45', importValue: 480, exportValue: 560 },
  ];

  protected readonly clusterChartData = [
    { country: 'AT', imports: 410, exports: 520 },
    { country: 'NL', imports: 630, exports: 580 },
    { country: 'CZ', imports: 310, exports: 290 },
    { country: 'FR', imports: 550, exports: 610 },
    { country: 'SW', imports: 120, exports: 180 },
    { country: 'NO', imports: 120, exports: 180 },
    { country: 'IT', imports: 120, exports: 180 },
    { country: 'DK', imports: 210, exports: 240 },
    { country: 'PL', imports: 480, exports: 430 },
    { country: 'CH', imports: 380, exports: 460 },
    { country: 'BG', imports: 340, exports: 390 },
  ];
}
