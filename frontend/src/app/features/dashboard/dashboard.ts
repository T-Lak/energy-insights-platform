import { Component, OnInit } from '@angular/core';

import { TooltipModule } from 'primeng/tooltip';

import { KpiSection } from './kpi-cards/kpi-cards';
import { ImportExportFlow } from './import-export-flow/import-export-flow';
import { CrossborderFlowMap } from './crossborder-flow-map/crossborder-flow-map';
import { BarChartItem } from '../../shared/components/bar-chart/bar-chart.model';
import { Widgets } from './widgets/widgets';
import { SubpageHeader } from '../../shared/components/subpage-header/subpage-header';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-dashboard',
  imports: [
    CommonModule,
    KpiSection,
    ImportExportFlow,
    TooltipModule,
    CrossborderFlowMap,
    Widgets,
    SubpageHeader,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  protected topSourcesData: BarChartItem[] = [
    { source: 'Solar PV', percentage: 34.2, label: '34.2% (12.4 GW)', color: '#5e70d7' },
    { source: 'Wind Offshore', percentage: 21.8, label: '21.8% (7.9 GW)', color: '#5e70d7' },
    { source: 'Biomass', percentage: 7.5, label: '7.5% (2.7 GW)', color: '#5e70d7' },
  ];
  protected carbonIntensityData: BarChartItem[] = [
    { source: 'Lignite', intensity: 1020, label: '1,020 g/kWh', color: '#a5a5a5' },
    { source: 'Hard Coal', intensity: 820, label: '820 g/kWh', color: '#a5a5a5' },
    { source: 'Gas Peakers', intensity: 490, label: '490 g/kWh', color: '#a5a5a5' },
  ];

  ngOnInit(): void {}
}
