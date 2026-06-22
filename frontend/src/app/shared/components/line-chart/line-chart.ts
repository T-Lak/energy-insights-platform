import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as am5 from '@amcharts/amcharts5';
import * as am5xy from '@amcharts/amcharts5/xy';
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated';
import { lineColors } from './line-chart.model';
import { Observable, Subscription } from 'rxjs';

@Component({
  selector: 'app-line-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './line-chart.html',
  styleUrl: './line-chart.scss',
})
export class LineChart implements OnInit, OnDestroy {
  @Input() data!: any[];
  @Input() timeseriesData$!: Observable<any[]>;
  @Input() dataUpdate$!: Observable<any>;

  private root!: am5.Root;

  private chart!: am5xy.XYChart;
  private xAxis!: am5xy.CategoryAxis<am5xy.AxisRenderer>;
  private series!: am5.Series[];
  private dataSubscription!: Subscription;
  private dataUpdateSubscription!: Subscription;
  private seriesData!: any[];

  ngOnInit(): void {
    if (this.timeseriesData$) {
      this.dataSubscription = this.timeseriesData$.subscribe({
        next: (unpackedData) => {
          this.seriesData = unpackedData;

          const chart = this.initChart();
          const [xAxis, yAxis] = this.initAxes(chart);
          const series = this.createSeries(chart, xAxis, yAxis);
          this.setCursor(chart, xAxis);

          this.chart = chart;
          this.xAxis = xAxis;
          this.series = series;

          if (this.seriesData && this.seriesData.length > 0) {
            this.setData(series, xAxis);
          }
        },
        error: (err) => console.error('Chart stream error:', err),
      });
    }

    if (this.dataUpdate$) {
      this.dataUpdateSubscription = this.dataUpdate$.subscribe({
        next: (newData) => {
          const existingIndex = this.seriesData.findIndex((item) => item.time === newData.time);

          if (existingIndex !== -1) {
            this.seriesData[existingIndex] = newData;
          } else {
            this.seriesData.push(newData);
          }

          if (this.seriesData.length > 28) {
            this.seriesData.shift();
          }

          this.setCursor(this.chart, this.xAxis);
          this.setData(this.series, this.xAxis);
        },
        error: (err) => console.error('Data update stream error:', err),
      });
    }
  }

  ngOnDestroy(): void {
    if (this.dataSubscription) this.dataSubscription.unsubscribe();
    if (this.dataUpdateSubscription) this.dataUpdateSubscription.unsubscribe();
    if (this.root) this.root.dispose();
  }

  private initChart(): am5xy.XYChart {
    this.root = am5.Root.new('splinechart');
    this.root.setThemes([am5themes_Animated.new(this.root)]);

    const chart = this.root.container.children.push(
      am5xy.XYChart.new(this.root, {
        panX: false,
        panY: false,
        wheelX: 'none',
        wheelY: 'none',
      }),
    );

    chart.set(
      'tooltip',
      am5.Tooltip.new(this.root, {
        getFillFromSprite: false,
        getStrokeFromSprite: false,
        autoTextColor: false,
      }),
    );

    chart.set('paddingBottom', 40);
    return chart;
  }

  private initAxes(
    chart: am5xy.XYChart,
  ): [am5xy.CategoryAxis<am5xy.AxisRenderer>, am5xy.ValueAxis<am5xy.AxisRenderer>] {
    const xAxis = chart.xAxes.push(
      am5xy.CategoryAxis.new(this.root, {
        maxDeviation: 0.5,
        categoryField: 'time',
        renderer: am5xy.AxisRendererX.new(this.root, { pan: 'zoom', minGridDistance: 50 }),
      }),
    );

    xAxis.get('renderer').labels.template.setAll({
      fill: am5.color('#888'),
      fontSize: '14px',
      fontWeight: '500',
      fontFamily: 'Inter, sans-serif',
      paddingTop: 8,
    });

    const yAxis = chart.yAxes.push(
      am5xy.ValueAxis.new(this.root, {
        maxDeviation: 1,
        extraMin: 0.1,
        extraMax: 0.1,
        renderer: am5xy.AxisRendererY.new(this.root, { pan: 'zoom' }),
      }),
    );

    xAxis.setAll({ startLocation: 0.1, endLocation: 0.9 });
    yAxis.get('renderer').labels.template.set('visible', false);
    yAxis.get('renderer').grid.template.setAll({ strokeOpacity: 0, visible: false });
    xAxis.get('renderer').grid.template.setAll({ strokeOpacity: 0, visible: false });

    return [xAxis, yAxis];
  }

  private createSeries(chart: am5xy.XYChart, xAxis: any, yAxis: any): am5.Series[] {
    const seriesList: am5.Series[] = [];

    const allKeys = Object.keys(this.seriesData[0]);
    const dataKeys = allKeys.filter((key) => key !== 'time' && key !== 'label' && key !== 'color');

    dataKeys.forEach((key, index) => {
      const colorHex = lineColors[index] || '#adb5bd';
      const color = am5.color(colorHex);
      const formattedName = key.charAt(0).toUpperCase() + key.slice(1);

      const s = chart.series.push(
        am5xy.SmoothedXLineSeries.new(this.root, {
          name: formattedName,
          xAxis,
          yAxis,
          valueYField: key,
          categoryXField: 'time',
          sequencedInterpolation: true,
          stroke: color,
          tooltip: this.createTooltip(key, color),
        }),
      );

      s.strokes.template.setAll({ strokeWidth: 2 });
      s.fills.template.setAll({ visible: true, fillOpacity: 0.08 });

      s.bullets.push(() => {
        const circle = am5.Circle.new(this.root, {
          radius: 4,
          fill: color,
          stroke: am5.color('#ffffff'),
          strokeWidth: 2,
          opacity: 0,
        });
        circle.states.create('working', { opacity: 1, scale: 1.2 });
        return am5.Bullet.new(this.root, { sprite: circle });
      });

      seriesList.push(s);
    });

    return seriesList;
  }

  private createTooltip(label: string, color: am5.Color): am5.Tooltip {
    const formattedLabel = label.charAt(0).toUpperCase() + label.slice(1);

    const customTooltip = am5.Tooltip.new(this.root, {
      pointerOrientation: 'horizontal',
      labelText: `${formattedLabel}: [bold]{valueY}[/] MW`,
      autoTextColor: false,
      getFillFromSprite: false,
    });

    customTooltip.get('background')?.setAll({
      fill: color,
      stroke: color,
      strokeWidth: 1,
      shadowColor: am5.color('#000000'),
      shadowBlur: 8,
      shadowOpacity: 0.15,
    });

    customTooltip.label.setAll({
      fill: am5.color('#ffffff'),
      fontSize: '12px',
      fontWeight: '500',
      fontFamily: 'Inter, sans-serif',
      paddingLeft: 6,
      paddingRight: 6,
      paddingTop: 4,
      paddingBottom: 4,
    });

    return customTooltip;
  }

  private setCursor(chart: am5xy.XYChart, xAxis: any): void {
    const cursor = chart.set(
      'cursor',
      am5xy.XYCursor.new(this.root, {
        behavior: 'none',
        xAxis,
      }),
    );
    cursor.lineY.set('visible', false);
  }

  private setData(series: am5.Series[], xAxis: any): void {
    xAxis.data.setAll(this.seriesData);

    for (let s of series) {
      s.data.setAll(this.seriesData);
    }
  }
}
