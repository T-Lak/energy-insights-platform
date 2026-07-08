import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

import * as L from 'leaflet';

import { FlowData, getBorderCoords } from './crossborder-flow-map.model';
import { Subscription } from 'rxjs';
import { FlowGridEdge } from '../../crossborder-flows/models/flow-grid-edge.model';
import { CrossborderFlowMapService } from './crossborder-flow-map.service';
import { getCountryFullName } from '../../../core/model/domain/country.model';
import { FlowDirection, getFlowColor } from '../../../core/model/domain/flows.model';

@Component({
  standalone: true,
  selector: 'app-energy-flow-map',
  imports: [CommonModule],
  providers: [CrossborderFlowMapService],
  templateUrl: './crossborder-flow-map.html',
  styleUrl: './crossborder-flow-map.scss',
})
export class CrossborderFlowMap implements OnInit, AfterViewInit, OnDestroy {
  private map!: L.Map;
  private markersLayer = L.layerGroup();
  private geoJsonLayerLoaded = false;
  private flowData: FlowData[] = [];
  private dataSubscription!: Subscription;

  constructor(
    private http: HttpClient,
    private flowService: CrossborderFlowMapService,
  ) {}

  ngOnInit(): void {
    this.dataSubscription = this.flowService.getFlowPoints('DE_LU').subscribe({
      next: (data) => {
        this.flowData = [];

        data.forEach((flowPoint: FlowGridEdge) => {
          this.flowData.push({
            from: getCountryFullName(flowPoint.fromRegion),
            to: getCountryFullName(flowPoint.toRegion),
            importValue: Math.round(flowPoint.importMW),
            exportValue: Math.round(flowPoint.exportMW),
            coords: getBorderCoords(flowPoint.toRegion),
          });
        });

        if (this.map && this.geoJsonLayerLoaded) {
          this.addFlowMarkers();
        }
      },
      error: (err) => console.error('Map data error:', err),
    });
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
    }, 50);
  }

  private initMap(): void {
    this.map = L.map('map', {
      center: [51.8, 10.4515],
      zoom: 5,
      preferCanvas: true,
      renderer: L.canvas({
        padding: 2,
      }),
      zoomControl: false,
      zoomAnimation: true,
      fadeAnimation: true,
      markerZoomAnimation: true,
      zoomAnimationThreshold: 20,
    });

    L.control.zoom({ position: 'topright' }).addTo(this.map);
    this.markersLayer.addTo(this.map);
    this.addResetControl();

    this.http.get('/europe.geojson').subscribe({
      next: (geoJsonData: any) => {
        L.geoJSON(geoJsonData, {
          style: {
            color: '#969ea5',
            weight: 1.5,
            fillColor: '#e7e8ec',
            fillOpacity: 1,
          },
        }).addTo(this.map);

        this.geoJsonLayerLoaded = true;

        if (this.flowData.length > 0) {
          this.addFlowMarkers();
        }

        this.map.setView([51.8, 10.4515], 5, { animate: false });

        this.map.invalidateSize();

        this.map.flyTo([51.8, 10.4515], 5.1, {
          duration: 0.5,
          easeLinearity: 0.5,
        });
      },
      error: (err) => {
        console.error(
          'Could not load europe.geojson from public folder. Check your console network tab!',
          err,
        );
      },
    });
  }

  private addFlowMarkers(): void {
    this.markersLayer.clearLayers();

    this.flowData.forEach((item) => {
      const netFlow = item.exportValue - item.importValue;

      const flowMarkerColor =
        netFlow > 0
          ? getFlowColor(FlowDirection.Export)
          : netFlow < 0
            ? getFlowColor(FlowDirection.Import)
            : getFlowColor(null as any);

      const marker = L.circleMarker(item.coords, {
        radius: 7,
        fillColor: flowMarkerColor,
        fillOpacity: 1,
        color: '#ffffff',
        weight: 2,
        interactive: true,
      }).addTo(this.map);

      let titleText: string;
      let flowDetails: string;

      if (netFlow > 0) {
        titleText = `${item.from} &rarr; ${item.to}`;
        flowDetails = `Exporting: <strong>${netFlow} MW</strong>`;
      } else if (netFlow < 0) {
        titleText = `${item.to} &rarr; ${item.from}`;
        flowDetails = `Importing: <strong>${Math.abs(netFlow)} MW</strong>`;
      } else {
        titleText = `${item.from} &harr; ${item.to}`;
        flowDetails = `No active exchange (0 MW)`;
      }

      marker.bindTooltip(
        `
        <div class="tooltip-header">${titleText}</div>
        <div class="tooltip-body">
          ${flowDetails}
        </div>
        `,
        {
          className: 'custom-flow-tooltip',
          direction: 'top',
          sticky: true,
        },
      );

      marker.addTo(this.markersLayer);
    });
  }

  private addResetControl() {
    const ResetViewControl = L.Control.extend({
      onAdd: () => {
        const container = L.DomUtil.create(
          'div',
          'leaflet-bar leaflet-control leaflet-control-custom',
        );

        container.innerHTML = '⟳';

        container.style.width = '24px';
        container.style.height = '24px';
        container.style.lineHeight = '24px';
        container.style.textAlign = 'center';
        container.style.cursor = 'pointer';
        container.style.background = '#6f7072';
        container.style.color = '#fff';
        container.style.fontSize = '14px';
        container.style.border = 'none';

        L.DomEvent.disableClickPropagation(container);

        container.onclick = () => {
          this.map.flyTo([51.8, 10.4515], 5.1, {
            duration: 1,
            easeLinearity: 0.25,
          });
        };

        return container;
      },
    });

    new ResetViewControl({ position: 'topright' }).addTo(this.map);
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }

    if (this.dataSubscription) {
      this.dataSubscription.unsubscribe();
    }
  }
}
