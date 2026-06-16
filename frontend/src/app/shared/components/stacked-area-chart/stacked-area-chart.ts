import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as am5 from '@amcharts/amcharts5';
import * as am5xy from '@amcharts/amcharts5/xy';
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated';

const RENEWABLE_COLORS: { [key: string]: string } = {
  solar: '#ffb703',
  windOnshore: '#00b4d8',
  windOffshore: '#0077b6',
  biomass: '#40916c',
  hydro: '#52b788',
};

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
export class StackedAreaChart implements OnChanges, OnDestroy {
  @Input() data!: any[];

  private root!: am5.Root;
  private seriesList: am5xy.LineSeries[] = [];
  private xAxis!: am5xy.CategoryAxis<am5xy.AxisRendererX>;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data'] && this.data && this.data.length > 0) {
      this.buildChart();
    }
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
      }),
    );

    // X-Axis (Time)
    const xAxisRenderer = am5xy.AxisRendererX.new(this.root, { minGridDistance: 60 });
    xAxisRenderer.labels.template.setAll({ fill: am5.color('#a2a1b5'), fontSize: 12 });
    xAxisRenderer.grid.template.setAll({ visible: false });

    this.xAxis = chart.xAxes.push(
      am5xy.CategoryAxis.new(this.root, {
        categoryField: 'time',
        renderer: xAxisRenderer,
        startLocation: 0,
        endLocation: 1,
      }),
    );

    // Y-Axis (Megawatts)
    const yAxisRenderer = am5xy.AxisRendererY.new(this.root, {});
    yAxisRenderer.labels.template.setAll({ fill: am5.color('#a2a1b5'), fontSize: 11 });

    const yAxis = chart.yAxes.push(
      am5xy.ValueAxis.new(this.root, {
        renderer: yAxisRenderer,
      }),
    );

    // Add Legend at the top
    const legend = chart.children.unshift(
      am5.Legend.new(this.root, {
        centerX: am5.p50,
        x: am5.p50,
        marginBottom: 15,
      }),
    );

    // Extract series keys dynamically (ignores 'time')
    const allKeys = Object.keys(this.data[0]);
    const seriesKeys = allKeys.filter((key) => key !== 'time');

    // Create stacked series
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
          categoryXField: 'time',
          stacked: true, // ◄ Triggers stacking physics!
          stroke: am5.color(colorHex),
          fill: am5.color(colorHex),
          tooltip: am5.Tooltip.new(this.root, {
            labelText: '{name}: [bold]{valueY}[/] MW',
          }),
        }),
      );

      // Turn the lines into shaded areas
      series.strokes.template.setAll({ strokeWidth: 1 });
      series.fills.template.setAll({ visible: true, fillOpacity: 0.7 });

      this.seriesList.push(series);
      legend.data.push(series);
    });

    // Add Cursor for multi-series crosshair tracking
    const cursor = chart.set('cursor', am5xy.XYCursor.new(this.root, { xAxis: this.xAxis }));
    cursor.lineY.set('visible', false);

    this.updateChartData();
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
