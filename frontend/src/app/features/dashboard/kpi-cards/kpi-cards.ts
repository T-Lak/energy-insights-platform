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
  protected tooltipDescriptions = [
    'The percentage of total domestic electricity generation produced by renewable energy sources (wind, solar, hydro, etc.).',
    'The volume of carbon dioxide emissions emitted per kilowatt-hour (gCO2/kWh) of electricity generated.',
    'The total real-time electrical power demand and consumption across the national transmission grid.',
    'The structural balance between total power exports and imports. Positive represents a net export; negative represents a net import.',
  ];
}
