import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';

import { ModuleRegistry, AllCommunityModule, ColDef, themeQuartz } from 'ag-grid-community';
import { AgGridAngular } from 'ag-grid-angular';
import { getColumnDefs } from './ag-table.model';

ModuleRegistry.registerModules([AllCommunityModule]);

@Component({
  selector: 'app-ag-table',
  standalone: true,
  imports: [CommonModule, AgGridAngular],
  templateUrl: './ag-table.html',
  styleUrl: './ag-table.scss',
})
export class AgTable implements OnInit, OnChanges {
  @Input() dataInput: any[] = [];
  @Input() granularity!: string;

  protected columnDefs: ColDef[] = [];
  protected rowData: any[] = [];

  readonly theme = themeQuartz.withParams({
    headerTextColor: '#555',
    headerBackgroundColor: '#e4e5e7',
    headerHeight: '40px',
    rowHeight: '40px',
    fontSize: '14px',
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['granularity']) {
      this.columnDefs = getColumnDefs(this.granularity);
    }

    if (changes['dataInput']) {
      this.rowData = changes['dataInput'].currentValue || [];
    }
  }

  defaultColDef: ColDef = {
    editable: false,
    flex: 1,
    minWidth: 100,
    filter: true,
  };

  ngOnInit() {}

  onGridReady(params: any) {
    params.api.sizeColumnsToFit();
  }
}
