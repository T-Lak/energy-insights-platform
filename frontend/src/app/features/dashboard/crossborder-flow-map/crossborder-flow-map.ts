import { Component, OnInit, OnDestroy } from '@angular/core';
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
export class CrossborderFlowMap implements OnInit, OnDestroy {
  private map!: L.Map;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.initMap();
  }

  private initMap(): void {
    this.map = L.map('map', {
      center: [51.1657, 10.4515],
      zoom: 5,
      zoomControl: false,
    });

    setTimeout(() => {
      this.map.invalidateSize();
    }, 500);

    L.control.zoom({ position: 'topright' }).addTo(this.map);

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

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }
}
