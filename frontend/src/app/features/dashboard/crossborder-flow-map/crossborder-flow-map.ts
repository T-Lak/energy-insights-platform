import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FlowData, getBorderCoords } from './crossborder-flow-map.model';
import { map, Observable } from 'rxjs';
import { FlowGridEdge } from '../../crossborder-flows/models/flow-grid-edge.model';
import { CrossborderFlowMapService } from './crossborder-flow-map.service';
import {
  COUNTRY_NAMES,
  getCountryCodes,
  getCountryFullName,
} from '../../../core/model/domain/country.model';

import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { Map } from '@shared/components/map/map';

@Component({
  standalone: true,
  selector: 'app-energy-flow-map',
  imports: [CommonModule, ProgressSpinnerModule, Map],
  providers: [CrossborderFlowMapService],
  templateUrl: './crossborder-flow-map.html',
  styleUrl: './crossborder-flow-map.scss',
})
export class CrossborderFlowMap implements OnInit {
  protected flowData$: Observable<any> | null = null;

  constructor(private flowService: CrossborderFlowMapService) {}

  ngOnInit(): void {
    this.flowData$ = this.flowService.getFlowPoints().pipe(
      map((flowPoints: FlowGridEdge[]) => {
        const allNeighbors = getCountryCodes('DE_LU');

        return allNeighbors.map((countryCode) => {
          const found = flowPoints.find((fp) => fp.toRegion === countryCode);

          return {
            from: 'Germany',
            to: COUNTRY_NAMES[countryCode],
            importValue: found ? Math.round(found.importMW) : 0,
            exportValue: found ? Math.round(found.exportMW) : 0,
            coords: getBorderCoords(countryCode),
          } as FlowData;
        });
      }),
    );
  }
}
