import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';

import { ModuleRegistry, AllCommunityModule, ColDef, themeQuartz } from 'ag-grid-community';
import { AgGridAngular } from 'ag-grid-angular';

ModuleRegistry.registerModules([AllCommunityModule]);

@Component({
  selector: 'app-ag-table',
  standalone: true,
  imports: [CommonModule, AgGridAngular],
  templateUrl: './ag-table.html',
  styleUrl: './ag-table.scss',
})
export class AgTable implements OnInit {
  readonly theme = themeQuartz.withParams({
    headerTextColor: '#555',
    headerBackgroundColor: '#e4e5e7',
    headerHeight: '40px',
    rowHeight: '40px',
    fontSize: '14px',
  });

  columnDefs: ColDef[] = [
    { field: 'country', headerName: 'Country' },
    { field: 'flowMw', headerName: 'Flow (MW)' },
    { field: 'flowDirection', headerName: 'Flow Direction' },
    { field: '1hChange', headerName: '1h Change (%)' },
    { field: '24hChange', headerName: '24h Change (%)' },
  ];

  defaultColDef: ColDef = {
    editable: false,
    flex: 1,
    minWidth: 100,
    filter: true,
  };

  rowData = [
    {
      country: 'France',
      flowMw: 1200,
      flowDirection: 'Export',
      '1hChange': 5,
      '24hChange': 10,
    },
    {
      country: 'Denmark',
      flowMw: 800,
      flowDirection: 'Import',
      '1hChange': -1,
      '24hChange': -2,
    },
    {
      country: 'Poland',
      flowMw: 500,
      flowDirection: 'Export',
      '1hChange': 0.5,
      '24hChange': 1,
    },
    {
      country: 'Czechia',
      flowMw: 1200,
      flowDirection: 'Export',
      '1hChange': 5,
      '24hChange': 10,
    },
    {
      country: 'Belgium',
      flowMw: 800,
      flowDirection: 'Import',
      '1hChange': -1,
      '24hChange': -2,
    },
    {
      country: 'Norway',
      flowMw: 500,
      flowDirection: 'Export',
      '1hChange': 0.5,
      '24hChange': 1,
    },
    {
      country: 'Netherlands',
      flowMw: 1200,
      flowDirection: 'Export',
      '1hChange': 5,
      '24hChange': 10,
    },
    {
      country: 'Sweden',
      flowMw: 800,
      flowDirection: 'Import',
      '1hChange': -1,
      '24hChange': -2,
    },
    {
      country: 'Austria',
      flowMw: 500,
      flowDirection: 'Export',
      '1hChange': 0.5,
      '24hChange': 1,
    },
    {
      country: 'Switzerland',
      flowMw: 1200,
      flowDirection: 'Export',
      '1hChange': 5,
      '24hChange': 10,
    },
    {
      country: 'DK',
      flowMw: 800,
      flowDirection: 'Import',
      '1hChange': -1,
      '24hChange': -2,
    },
    {
      country: 'PL',
      flowMw: 500,
      flowDirection: 'Export',
      '1hChange': 0.5,
      '24hChange': 1,
    },
    {
      country: 'FR',
      flowMw: 1200,
      flowDirection: 'Export',
      '1hChange': 0.5,
      '24hChange': 1,
    },
    {
      country: 'FR',
      flowMw: 1200,
      flowDirection: 'Export',
      '1hChange': 5,
      '24hChange': 10,
    },
    {
      country: 'DK',
      flowMw: 800,
      flowDirection: 'Import',
      '1hChange': -1,
      '24hChange': -2,
    },
    {
      country: 'PL',
      flowMw: 500,
      flowDirection: 'Export',
      '1hChange': 0.5,
      '24hChange': 1,
    },
    {
      country: 'FR',
      flowMw: 1200,
      flowDirection: 'Export',
      '1hChange': 0.5,
      '24hChange': 1,
    },
    {
      country: 'FR',
      flowMw: 1200,
      flowDirection: 'Export',
      '1hChange': 5,
      '24hChange': 10,
    },
    {
      country: 'DK',
      flowMw: 800,
      flowDirection: 'Import',
      '1hChange': -1,
      '24hChange': -2,
    },
    {
      country: 'PL',
      flowMw: 500,
      flowDirection: 'Export',
      '1hChange': 0.5,
      '24hChange': 1,
    },
  ];

  ngOnInit() {}

  onGridReady(params: any) {
    params.api.sizeColumnsToFit();
  }
}
