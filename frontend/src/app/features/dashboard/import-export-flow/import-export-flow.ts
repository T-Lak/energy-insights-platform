import { Component, OnInit } from '@angular/core';
import { LineChart } from '../../../shared/components/line-chart/line-chart';
import { CommonModule } from '@angular/common';
import { Observable, map, tap } from 'rxjs';
import { ImportExportFlowService } from './import-export-flow.service';
import { CrossborderFlowTotalsTsPayload } from '../../../core/model/payload/crossborder-flow-totals-ts.payload';
import { FlowTotalsDTO } from '../../../core/model/dto/flow-totals.dto';

@Component({
  selector: `app-import-export-flow`,
  standalone: true,
  imports: [CommonModule, LineChart],
  providers: [ImportExportFlowService],
  templateUrl: './import-export-flow.html',
  styleUrl: './import-export-flow.scss',
})
export class ImportExportFlow implements OnInit {
  protected flowTotals$!: Observable<any>;
  protected flowTotalsTs$!: Observable<any[]>;
  protected flowTotalsUpdate$!: Observable<any>;

  protected data = [
    { time: '10:00', importValue: 1200, exportValue: 600 },
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
    { time: '17:00', importValue: 1100, exportValue: 650 },
  ];

  constructor(private importExportFlowService: ImportExportFlowService) {}

  ngOnInit(): void {
    this.flowTotalsTs$ = this.importExportFlowService
      .getFlowTotalsTimeseries(7)
      .pipe(map((payload) => this.transformData(payload)));

    this.flowTotalsUpdate$ = this.importExportFlowService.getLiveFlowTotals().pipe(
      map((point) => this.transformLivePoint(point)),
      tap((value) => console.log('NEW LIVE POINT ARRIVED:', value)),
    );
  }

  transformData(flow: CrossborderFlowTotalsTsPayload) {
    return flow.flowTotals.map((p: any) => this.mapPoint(p));
  }

  transformLivePoint(point: FlowTotalsDTO) {
    return this.mapPoint(point);
  }

  private mapPoint(p: any) {
    return {
      time: new Date(p.timestamp).toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit',
      }),
      importValue: p.totalImportMW ? Number(p.totalImportMW.toFixed(2)) : 0,
      exportValue: p.totalExportMW ? Number(p.totalExportMW.toFixed(2)) : 0,
    };
  }
}
