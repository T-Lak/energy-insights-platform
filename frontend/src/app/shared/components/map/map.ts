import {
  Component,
  AfterViewInit,
  Input,
  OnDestroy,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import maplibregl, { Popup, NavigationControl } from 'maplibre-gl';
import { FlowData } from '../../../features/dashboard/crossborder-flow-map/crossborder-flow-map.model';
import { CommonModule } from '@angular/common';

import { ChangeDetectorRef } from '@angular/core';
import { FlowDirection, getFlowColor } from '../../../core/model/domain/flows.model';

@Component({
  standalone: true,
  selector: 'app-map',
  imports: [CommonModule],
  templateUrl: './map.html',
  styleUrl: './map.scss',
})
export class Map implements AfterViewInit, OnDestroy, OnChanges {
  @Input() public inputData!: FlowData[];
  private map!: maplibregl.Map;
  private popup = new Popup({ closeButton: false, closeOnClick: false });

  public hoveredData: any | null = null;
  public tooltipX = 0;
  public tooltipY = 0;

  constructor(private cdr: ChangeDetectorRef) {}

  ngAfterViewInit(): void {
    this.map = new maplibregl.Map({
      container: 'map',
      style: {
        version: 8,
        sources: {
          carto: {
            type: 'raster',
            tiles: ['https://cartodb-basemaps-a.global.ssl.fastly.net/light_all/{z}/{x}/{y}.png'],
            tileSize: 75,
          },
        },
        layers: [{ id: 'carto', type: 'raster', source: 'carto' }],
      },
      center: [10.4515, 51.8],
      zoom: 4,
    });

    this.map.on('load', () => {
      if (this.inputData) this.addFlowMarkers();

      if (this.map.getLayer('background')) {
        this.map.setPaintProperty('background', 'background-color', 'rgba(0,0,0,0)');
      }
    });
  }

  private createResetControl(): any {
    class ResetControl {
      onAdd(map: maplibregl.Map) {
        const div = document.createElement('div');
        div.className = 'maplibregl-ctrl maplibregl-ctrl-group';
        div.innerHTML = `<button title="Reset View" style="cursor:pointer; font-size:16px;">⟳</button>`;
        div.onclick = () => map.flyTo({ center: [10.4515, 51.8], zoom: 5 });
        return div;
      }
    }
    return new ResetControl();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['inputData'] && this.map?.isStyleLoaded()) {
      this.addFlowMarkers();
    }
  }

  private addFlowMarkers(): void {
    if (!this.inputData) return;

    if (this.map.getLayer('flow-markers')) this.map.removeLayer('flow-markers');
    if (this.map.getSource('flows')) this.map.removeSource('flows');

    this.map.addSource('flows', {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: this.inputData.map((d) => {
          const netFlow = d.exportValue - d.importValue;
          const color =
            netFlow > 0
              ? getFlowColor(FlowDirection.Export)
              : netFlow < 0
                ? getFlowColor(FlowDirection.Import)
                : getFlowColor(null as any);

          return {
            type: 'Feature',
            geometry: {
              type: 'Point',
              coordinates: [(d.coords as [number, number])[1], (d.coords as [number, number])[0]],
            },
            properties: {
              from: d.from,
              to: d.to,
              netFlow: netFlow,
              color: color,
            },
          };
        }),
      },
    });

    this.map.addLayer({
      id: 'flow-markers',
      type: 'circle',
      source: 'flows',
      paint: {
        'circle-radius': 7,
        'circle-color': ['get', 'color'],
        'circle-stroke-width': 2,
        'circle-stroke-color': '#ffffff',
      },
    });

    this.map.on('mouseenter', 'flow-markers', (e) => {
      this.map.getCanvas().style.cursor = 'pointer';
      this.hoveredData = e.features![0].properties as FlowData;
      this.cdr.detectChanges(); // Force update
    });

    this.map.on('mousemove', 'flow-markers', (e) => {
      this.tooltipX = e.originalEvent.clientX + 15;
      this.tooltipY = e.originalEvent.clientY + 15;
      this.cdr.detectChanges();
    });

    this.map.on('mouseleave', 'flow-markers', () => {
      this.map.getCanvas().style.cursor = '';
      this.hoveredData = null;
      this.cdr.detectChanges();
    });
  }

  public resetMap(): void {
    if (this.map) {
      this.map.flyTo({ center: [10.4515, 51.8], zoom: 4 });
    }
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }
}
