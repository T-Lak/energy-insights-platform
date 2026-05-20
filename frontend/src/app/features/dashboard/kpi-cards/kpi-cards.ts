import { Component } from '@angular/core';
import { KpiType } from '../dashboard.model';
import { KpiCard } from './kpi-card/kpi-card';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-kpi-section',
  standalone: true,
  imports: [CommonModule, KpiCard],
  templateUrl: './kpi-cards.html',
  styleUrl: './kpi-cards.scss',
})
export class KpiSection {
  protected readonly KpiType = KpiType;
  protected readonly kpiList = Object.values(KpiType);
}
