import { Component, OnInit } from '@angular/core';

import { Widget } from './widget/widget';

import { BarChart } from '../../../shared/components/bar-chart/bar-chart';
import { BarChartItem } from '../../../shared/components/bar-chart/bar-chart.model';
import { CommonModule } from '@angular/common';
import { SourceRankingPointDTO } from '../../../core/model/dto/source-ranking-point.dto';
import { Observable, switchMap, map } from 'rxjs';
import { WidgetsService } from './widgets.service';
import { TopSourcesCategory } from '../dashboard.model';

@Component({
  selector: 'app-widgets',
  standalone: true,
  imports: [CommonModule, Widget, BarChart],
  providers: [WidgetsService], // Re-created on component init, which is fine if shared!
  templateUrl: './widgets.html',
  styleUrl: './widgets.scss',
})
export class Widgets implements OnInit {
  protected readonly TopSourcesCategory = TopSourcesCategory;
  protected dictionaryStream$!: Observable<{
    [key in TopSourcesCategory]: Observable<SourceRankingPointDTO[]>;
  }>;

  constructor(private widgetsService: WidgetsService) {}

  ngOnInit(): void {
    // Single HTTP pipeline invocation point!
    this.dictionaryStream$ = this.widgetsService.getWidgetsData();
  }
}
