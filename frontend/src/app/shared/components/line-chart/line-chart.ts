import {
  Component,
  Input,
  OnDestroy,
  OnChanges,
  SimpleChanges,
  AfterViewInit,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import * as am5 from '@amcharts/amcharts5';
import * as am5xy from '@amcharts/amcharts5/xy';
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated';
import { FlowDirection, getFlowColor } from '../../../core/model/domain/flows.model';

@Component({
  selector: 'app-line-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './line-chart.html',
  styleUrl: './line-chart.scss',
})
export class LineChart implements OnChanges, OnDestroy, AfterViewInit {
  @Input() granularity: string = 'daily';
  @Input() seriesData!: any[];

  private root!: am5.Root;

  private chart!: am5xy.XYChart;
  private xAxis!: am5xy.DateAxis<am5xy.AxisRenderer>;
  private series!: am5.Series[];

  private isViewInitialized = false;

  ngOnChanges(changes: SimpleChanges): void {
    console.log(changes);
    if (!changes['seriesData']) return;
    this.seriesData = changes['seriesData'].currentValue;

    if (!this.isViewInitialized) return;

    this.tryUpdate();
  }

  ngAfterViewInit() {
    this.isViewInitialized = true;

    if (!this.root && this.seriesData?.length) {
      this.initFullChart();
    }

    this.tryUpdate();
  }

  private tryUpdate(): void {
    if (!this.seriesData?.length) return;

    if (!this.root) {
      this.initFullChart();
    }

    if (this.series?.length && this.xAxis) {
      this.setData(this.series, this.xAxis);
    }
  }

  private initFullChart(): void {
    this.chart = this.initChart();

    const [xAxis, yAxis] = this.initAxes(this.chart);
    this.xAxis = xAxis;

    this.series = this.createSeries(this.chart, xAxis, yAxis);

    this.setCursor(this.chart, xAxis);
  }
  ngOnDestroy(): void {
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
  ): [am5xy.DateAxis<am5xy.AxisRenderer>, am5xy.ValueAxis<am5xy.AxisRenderer>] {
    const xAxis = chart.xAxes.push(
      am5xy.DateAxis.new(this.root, {
        maxDeviation: 0.5,
        baseInterval: { timeUnit: 'hour', count: 1 },
        renderer: am5xy.AxisRendererX.new(this.root, { pan: 'zoom', minGridDistance: 70 }),
      }),
    );

    xAxis.get('renderer').labels.template.setAll({
      text: '{displayTime}',
      fill: am5.color('#888'),
      fontSize: '14px',
      fontWeight: '500',
      fontFamily: 'Inter, sans-serif',
      paddingTop: 8,
    });

    const yAxis = chart.yAxes.push(
      am5xy.ValueAxis.new(this.root, {
        maxDeviation: 1,
        min: 0,
        extraMin: 0,
        extraMax: 0.1,
        renderer: am5xy.AxisRendererY.new(this.root, { pan: 'zoom' }),
      }),
    );

    xAxis.setAll({ startLocation: 0.5, endLocation: 0.5 });
    yAxis.get('renderer').labels.template.set('visible', false);
    yAxis.get('renderer').grid.template.setAll({ strokeOpacity: 0, visible: false });
    xAxis.get('renderer').grid.template.setAll({ strokeOpacity: 0, visible: false });

    return [xAxis, yAxis];
  }

  private createSeries(chart: am5xy.XYChart, xAxis: any, yAxis: any): am5.Series[] {
    const seriesList: am5.Series[] = [];

    const allKeys = Object.keys(this.seriesData[0]);

    const dataKeys = allKeys.filter(
      (key) =>
        key !== 'time' &&
        key !== 'timestamp' &&
        key !== 'displayTime' &&
        key !== 'label' &&
        key !== 'color',
    );

    dataKeys.forEach((key) => {
      const colorHex =
        key.toLocaleLowerCase() === 'import'
          ? getFlowColor(FlowDirection.Import)
          : getFlowColor(FlowDirection.Export);
      const color = am5.color(colorHex);
      const formattedName = key.charAt(0).toUpperCase() + key.slice(1);

      const s = chart.series.push(
        am5xy.SmoothedXLineSeries.new(this.root, {
          name: formattedName,
          xAxis,
          yAxis,
          valueYField: key,
          valueXField: 'time',
          sequencedInterpolation: true,
          connect: false,
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
    let interval = { timeUnit: 'hour', count: 1 };

    if (this.granularity === 'quarter-hourly') {
      interval = { timeUnit: 'minute', count: 15 };
    } else if (this.granularity === 'daily') {
      interval = { timeUnit: 'hour', count: 1 };
    } else if (this.granularity === 'weekly') {
      interval = { timeUnit: 'hour', count: 1 };
    } else if (this.granularity === 'monthly') {
      interval = { timeUnit: 'day', count: 1 };
    }

    xAxis.set('baseInterval', interval);

    xAxis.data.setAll(this.seriesData);
    for (let s of series) {
      s.data.setAll(this.seriesData);
    }
  }
}
