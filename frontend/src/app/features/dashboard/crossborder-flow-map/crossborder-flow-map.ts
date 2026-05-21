import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

import * as L from 'leaflet';

import { flowData } from './crossborder-flow-map.model';

@Component({
  standalone: true,
  selector: 'app-energy-flow-map',
  imports: [CommonModule],
  templateUrl: './crossborder-flow-map.html',
  styleUrl: './crossborder-flow-map.scss',
})
export class CrossborderFlowMap implements AfterViewInit, OnDestroy {
  private map!: L.Map;

  constructor(private http: HttpClient) {}

  ngAfterViewInit(): void {
    this.initMap();
  }

  private initMap(): void {
    this.map = L.map('map', {
      center: [51.1657, 10.4515],
      zoom: 5,
      zoomControl: false,
      zoomAnimation: true,
      fadeAnimation: true,
      markerZoomAnimation: true,
      zoomAnimationThreshold: 20,
    });

    L.control.zoom({ position: 'topright' }).addTo(this.map);
    this.addResetControl();

    this.http.get('/europe.geojson').subscribe({
      next: (geoJsonData: any) => {
        L.geoJSON(geoJsonData, {
          style: {
            color: '#adb5bd',
            weight: 1.5,
            fillColor: '#e7e8ec',
            fillOpacity: 1,
          },
        }).addTo(this.map);

        this.addFlowMarkers();

        this.map.setView([51.1657, 10.4515], 5, { animate: false });

        setTimeout(() => {
          this.map.flyTo([51.1657, 10.4515], 5.1, {
            duration: 0.5,
            easeLinearity: 0.5,
          });
        }, 100);
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
    flowData.forEach((item) => {
      const marker = L.circleMarker(item.coords, {
        radius: 7,
        fillColor: '#5e70d7',
        fillOpacity: 1,
        color: '#ffffff',
        weight: 2,
        interactive: true,
      }).addTo(this.map);

      marker.bindTooltip(
        `<strong>${item.from} &rarr; ${item.to}</strong><br/>Import: ${item.importValue} MW<br/>Export: ${item.exportValue} MW`,
        {
          className: 'custom-flow-tooltip',
          direction: 'top',
          sticky: true,
        },
      );
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
          this.map.flyTo([51.1657, 10.4515], 5.1, {
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
  }
}
