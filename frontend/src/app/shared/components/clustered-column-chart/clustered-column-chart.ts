import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as am5 from '@amcharts/amcharts5';
import * as am5xy from '@amcharts/amcharts5/xy';
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated';

const DEFAULT_COLORS = ['#5e70d7', '#10b981'];

@Component({
  selector: 'app-clustered-column-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './clustered-column-chart.html',
  styleUrl: './clustered-column-chart.scss',
})
export class ClusteredColumnChart implements OnChanges, OnDestroy {
  @Input() data!: any[];
  @Input() categoryField: string = 'country';

  private root!: am5.Root;
  private seriesList: am5xy.ColumnSeries[] = [];
  private xAxis!: any;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data'] && this.data && this.data.length > 0) {
      this.buildChart();
    }
  }

  ngOnDestroy(): void {
    if (this.root) {
      this.root.dispose();
    }
    this.seriesList = [];
  }

  private buildChart(): void {
    this.root = am5.Root.new('columnChart');
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

    const [xAxis, yAxis] = this.initAxes(chart);
    this.xAxis = xAxis;

    const allKeys = Object.keys(this.data[0]);
    const seriesKeys = allKeys.filter(
      (key) => key !== this.categoryField && key !== 'label' && key !== 'color',
    );

    seriesKeys.forEach((key, index) => {
      const colorHex = this.data[0].color?.[key] || DEFAULT_COLORS[index] || '#adb5bd';
      const formattedName = key.charAt(0).toUpperCase() + key.slice(1);

      const series = this.createSeries(
        chart,
        xAxis,
        yAxis,
        formattedName,
        key,
        am5.color(colorHex),
      );
      this.seriesList.push(series);
    });

    this.updateChartData();
  }

  private initAxes(chart: am5xy.XYChart) {
    const xAxisRenderer = am5xy.AxisRendererX.new(this.root, {
      minGridDistance: 30,
      cellStartLocation: 0.1,
      cellEndLocation: 0.9,
    });

    xAxisRenderer.labels.template.setAll({ fill: am5.color('#888'), fontSize: 14, paddingTop: 10 });
    xAxisRenderer.grid.template.setAll({ strokeOpacity: 0, visible: false });

    const xAxis = chart.xAxes.push(
      am5xy.CategoryAxis.new(this.root, {
        categoryField: this.categoryField,
        renderer: xAxisRenderer,
      }),
    );

    const yAxisRenderer = am5xy.AxisRendererY.new(this.root, {});
    yAxisRenderer.labels.template.setAll({ fill: am5.color('#888'), fontSize: 14 });

    const yAxis = chart.yAxes.push(
      am5xy.ValueAxis.new(this.root, {
        renderer: yAxisRenderer,
      }),
    );

    return [xAxis, yAxis];
  }

  private createSeries(
    chart: am5xy.XYChart,
    xAxis: any,
    yAxis: any,
    name: string,
    fieldName: string,
    color: am5.Color,
  ): am5xy.ColumnSeries {
    const series = chart.series.push(
      am5xy.ColumnSeries.new(this.root, {
        name: name,
        xAxis: xAxis,
        yAxis: yAxis,
        valueYField: fieldName,
        categoryXField: this.categoryField,
        clustered: true,
      }),
    );

    series.columns.template.setAll({
      width: am5.percent(90),
      fill: color,
      strokeOpacity: 0,
      cornerRadiusTL: 4,
      cornerRadiusTR: 4,
    });

    series.bullets.push(() => {
      return am5.Bullet.new(this.root, {
        locationY: 1,
        sprite: am5.Label.new(this.root, {
          text: '{valueY}',
          fill: am5.color('#ffffff'),
          centerY: am5.p0,
          centerX: am5.p50,
          populateText: true,
          fontSize: '9px',
          fontWeight: '600',
          fontFamily: 'Inter, sans-serif',
        }),
      });
    });

    return series;
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
