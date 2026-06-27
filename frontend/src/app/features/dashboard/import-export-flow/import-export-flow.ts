import { Component, OnDestroy, OnInit } from '@angular/core';
import { LineChart } from '../../../shared/components/line-chart/line-chart';
import { CommonModule } from '@angular/common';
import { map, Subscription, take } from 'rxjs';
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
export class ImportExportFlow implements OnInit, OnDestroy {
  protected chartData: any[] = [];

  private initialDataSubscription!: Subscription;
  private webSocketDataSubscription!: Subscription;

  constructor(private importExportFlowService: ImportExportFlowService) {}

  ngOnInit(): void {
    this.initialDataSubscription = this.importExportFlowService
      .getFlowTotalsTimeseries(7)
      .pipe(
        map((payload) => this.transformData(payload)),
        take(1),
      )
      .subscribe({
        next: (data) => {
          this.chartData = data;
        },
      });

    this.webSocketDataSubscription = this.importExportFlowService
      .getLiveFlowTotals()
      .pipe(map((point) => this.transformLivePoint(point)))
      .subscribe({
        next: (data) => {
          const updatedChartData = [...this.chartData];
          const existingIndex = updatedChartData.findIndex((item) => item.time === data.time);

          if (existingIndex !== -1) {
            updatedChartData[existingIndex] = data;
          } else {
            updatedChartData.push(data);
          }

          if (updatedChartData.length > 28) {
            updatedChartData.shift();
          }

          this.chartData = updatedChartData;
        },
      });
  }

  ngOnDestroy(): void {
    if (this.initialDataSubscription) {
      this.initialDataSubscription.unsubscribe();
    }
    if (this.webSocketDataSubscription) {
      this.webSocketDataSubscription.unsubscribe();
    }
  }

  private transformData(flow: CrossborderFlowTotalsTsPayload) {
    return flow.flowTotals.map((p: any) => this.mapPoint(p));
  }

  private transformLivePoint(point: FlowTotalsDTO) {
    return this.mapPoint(point);
  }

  private mapPoint(p: any) {
    const date = new Date(p.timestamp);

    const cleanTime = date.toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });

    const uniqueId = `${cleanTime} (${date.toLocaleDateString([], { day: '2-digit', month: '2-digit' })})`;

    return {
      timestamp: p.timestamp,
      time: uniqueId,
      displayTime: cleanTime,
      importValue: p.totalImportMW ? Number(p.totalImportMW.toFixed(2)) : 0,
      exportValue: p.totalExportMW ? Number(p.totalExportMW.toFixed(2)) : 0,
    };
  }
}
