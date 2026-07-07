import { Component, OnInit } from '@angular/core';

import { Widget } from './widget/widget';

import { BarChart } from '../../../shared/components/bar-chart/bar-chart';
import { CommonModule } from '@angular/common';
import { SourceRankingPointDTO } from '../../../core/model/dto/source-ranking-point.dto';
import { Observable } from 'rxjs';
import { WidgetsService } from './widgets.service';
import { TopSourcesCategory } from '../dashboard.model';

@Component({
  selector: 'app-widgets',
  standalone: true,
  imports: [CommonModule, Widget, BarChart],
  providers: [WidgetsService],
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
    this.dictionaryStream$ = this.widgetsService.getWidgetsData();
  }
}
