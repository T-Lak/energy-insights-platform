import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
  AfterViewInit,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import * as am5 from '@amcharts/amcharts5';
import * as am5xy from '@amcharts/amcharts5/xy';
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated';
import { RENEWABLE_COLORS } from '../../../core/model/domain/sources.model';

@Component({
  selector: 'app-stacked-area-chart',
  standalone: true,
  imports: [CommonModule],
  template: `<div id="stackedAreaChart" style="width: 100%; height: 100%;"></div>`,
  styles: [
    `
      :host {
        display: block;
        height: 100%;
        width: 100%;
      }
    `,
  ],
})
export class StackedAreaChart implements OnChanges, AfterViewInit, OnDestroy {
  @Input() data!: any[];

  private root!: am5.Root;
  private seriesList: am5xy.LineSeries[] = [];
  private xAxis!: am5xy.DateAxis<am5xy.AxisRendererX>;

  private isViewInitialized = false;

  private noDataIndicator: am5.Modal | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data']) {
      this.data = changes['data'].currentValue || [];
    }

    if (!this.isViewInitialized) return;

    if (this.data.length === 0) {
      this.seriesList.forEach((series) => series.data.setAll([]));
      this.xAxis.data.setAll([]);
    } else {
      this.tryUpdate();
    }

    this.toggleNoDataIndicator(this.data.length === 0);
  }

  ngAfterViewInit(): void {
    this.isViewInitialized = true;

    if (!this.root && this.data?.length) {
      this.buildChart();
    }

    this.tryUpdate();
  }

  private tryUpdate(): void {
    if (!this.data?.length) return;

    if (!this.root) {
      this.buildChart();
    }

    console.log('Data: ', this.data);

    this.updateChartData();
  }

  ngOnDestroy(): void {
    this.destroyChart();
  }

  private destroyChart(): void {
    if (this.root) {
      this.root.dispose();
    }
    this.seriesList = [];
  }

  private buildChart(): void {
    this.destroyChart();

    this.root = am5.Root.new('stackedAreaChart');
    this.root.setThemes([am5themes_Animated.new(this.root)]);

    const chart = this.root.container.children.push(
      am5xy.XYChart.new(this.root, {
        panX: false,
        panY: false,
        wheelX: 'none',
        wheelY: 'none',
        layout: this.root.verticalLayout,
        maxTooltipDistance: 1,
      }),
    );

    const xAxisRenderer = am5xy.AxisRendererX.new(this.root, { minGridDistance: 60 });
    xAxisRenderer.labels.template.setAll({ fill: am5.color('#888'), fontSize: 14 });
    xAxisRenderer.grid.template.setAll({ visible: false });

    this.xAxis = chart.xAxes.push(
      am5xy.DateAxis.new(this.root, {
        baseInterval: { timeUnit: 'minute', count: 1 },
        renderer: xAxisRenderer,
        startLocation: 0,
        endLocation: 1,
      }),
    );

    const yAxisRenderer = am5xy.AxisRendererY.new(this.root, {});
    yAxisRenderer.labels.template.setAll({ fill: am5.color('#888'), fontSize: 14 });

    yAxisRenderer.labels.template.adapters.add('text', (text, target) => {
      const dataItem = target.dataItem as am5.DataItem<am5xy.IValueAxisDataItem>;

      if (dataItem && dataItem.get('value') === 0) {
        return '';
      }
      return text;
    });

    const yAxis = chart.yAxes.push(
      am5xy.ValueAxis.new(this.root, {
        renderer: yAxisRenderer,
      }),
    );

    const legend = chart.children.unshift(
      am5.Legend.new(this.root, {
        centerX: am5.p50,
        x: am5.p50,
        marginBottom: 40,
      }),
    );

    legend.labels.template.setAll({
      fontSize: 13,
    });

    const allKeys = Object.keys(this.data[0]);
    const seriesKeys = allKeys.filter((key) => key !== 'timestamp');

    seriesKeys.forEach((key) => {
      const colorHex = RENEWABLE_COLORS[key] || '#adb5bd';
      const formattedName = key
        .replace(/([A-Z])/g, ' $1')
        .replace(/^./, (str) => str.toUpperCase());

      const series = chart.series.push(
        am5xy.LineSeries.new(this.root, {
          name: formattedName,
          xAxis: this.xAxis,
          yAxis: yAxis,
          valueYField: key,
          valueXField: 'timestamp',
          stacked: true,
          stroke: am5.color(colorHex),
          fill: am5.color(colorHex),
          tooltip: this.createTooltip(formattedName, am5.color(colorHex)),
        }),
      );

      series.strokes.template.setAll({ strokeWidth: 1 });
      series.fills.template.setAll({ visible: true, fillOpacity: 0.7 });

      this.seriesList.push(series);
      legend.data.push(series);
    });

    // const cursor = chart.set(
    //   'cursor',
    //   am5xy.XYCursor.new(this.root, { xAxis: this.xAxis, snapToSeriesBy: 'x' }),
    // );
    // cursor.lineY.set('visible', false);

    this.updateChartData();
  }

  private createTooltip(key: string, color: am5.Color): am5.Tooltip {
    const customTooltip = am5.Tooltip.new(this.root, {
      pointerOrientation: 'horizontal',
      labelText: `${key}: [bold]{valueY}[/] MW`,
      autoTextColor: false,
      getFillFromSprite: false,
    });

    customTooltip.get('background')?.setAll({
      fill: color,
      stroke: am5.color('#cacaca'),
      strokeWidth: 1,
      shadowColor: am5.color('#000000'),
      shadowBlur: 12,
      shadowOpacity: 0.25,
    });

    customTooltip.label.setAll({
      fill: am5.color('#ffffff'),
      fontSize: '12px',
      fontWeight: '500',
      fontFamily: 'Inter, sans-serif',
      paddingLeft: 2,
      paddingRight: 2,
      paddingTop: 1,
      paddingBottom: 1,
    });

    return customTooltip;
  }

  private toggleNoDataIndicator(show: boolean): void {
    if (!this.noDataIndicator && this.root) {
      this.noDataIndicator = am5.Modal.new(this.root, {
        content: `
        <div style="
          text-align: center; 
          font-family: 'Inter', sans-serif; 
          color: #6a6970;
          font-size: 14px;
          font-weight: 500;
        ">
          No data available for this date yet
        </div>
      `,
      });

      const contentDiv = this.noDataIndicator.getPrivate('content');

      if (contentDiv) {
        contentDiv.style.backgroundColor = '#ffffff';
        contentDiv.style.padding = '20px 30px';
        contentDiv.style.borderRadius = '8px';
        contentDiv.style.boxShadow = '0 8px 24px rgba(0, 0, 0, 0.15)';
        contentDiv.style.border = '1px solid #ccc';
      }
    }

    if (this.noDataIndicator) {
      if (show) {
        this.noDataIndicator.open();
      } else {
        this.noDataIndicator.close();
      }
    }
  }

  private updateChartData(): void {
    if (this.data && this.data.length > 0 && this.xAxis) {
      this.xAxis.data.setAll(this.data);
      this.seriesList.forEach((series) => {
        series.data.setAll(this.data);
      });
    }
  }
}
