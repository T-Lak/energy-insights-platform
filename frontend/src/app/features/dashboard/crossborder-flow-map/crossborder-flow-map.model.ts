import { LatLngExpression } from 'leaflet';

export interface FlowData {
  from: string;
  to: string;
  importValue: number;
  exportValue: number;
  coords: LatLngExpression;
}

const BORDER_COORDS: Record<string, LatLngExpression> = {
  AT: [47.7, 12.1] as LatLngExpression,
  BE: [50.6, 6.2] as LatLngExpression,
  CH: [47.7, 8.6] as LatLngExpression,
  CZ: [50.2, 12.5] as LatLngExpression,
  DK: [54.8, 9.3] as LatLngExpression,
  FR: [49, 8] as LatLngExpression,
  NL: [52.1, 6.9] as LatLngExpression,
  NO: [55, 6] as LatLngExpression,
  PL: [52.3, 14.7] as LatLngExpression,
  SE: [55, 13.5] as LatLngExpression,
};

export function getBorderCoords(countryAbbr: string): LatLngExpression {
  if (!countryAbbr) return [0, 0] as LatLngExpression;

  return BORDER_COORDS[countryAbbr];
}

export const flowData: FlowData[] = [
  {
    from: 'Germany',
    to: 'France',
    importValue: 1200,
    exportValue: 1200,
    coords: [49, 8] as L.LatLngExpression,
  },
  {
    from: 'Germany',
    to: 'Denmark',
    importValue: 800,
    exportValue: 1200,
    coords: [54.8, 9.3] as L.LatLngExpression,
  },
  {
    from: 'Germany',
    to: 'Sweden',
    importValue: 600,
    exportValue: 1200,
    coords: [55, 13.5] as L.LatLngExpression,
  },
  {
    from: 'Germany',
    to: 'Norway',
    importValue: 600,
    exportValue: 1200,
    coords: [55, 6] as L.LatLngExpression,
  },
  {
    from: 'Germany',
    to: 'Poland',
    importValue: 500,
    exportValue: 1200,
    coords: [52.3, 14.7] as L.LatLngExpression,
  },
  {
    from: 'Germany',
    to: 'Czechia',
    importValue: 750,
    exportValue: 1200,
    coords: [50.2, 12.5] as L.LatLngExpression,
  },
  {
    from: 'Germany',
    to: 'Austria',
    importValue: 1500,
    exportValue: 1200,
    coords: [47.7, 12.1] as L.LatLngExpression,
  },
  {
    from: 'Germany',
    to: 'Switzerland',
    importValue: 950,
    exportValue: 1200,
    coords: [47.7, 8.6] as L.LatLngExpression,
  },
  {
    from: 'Germany',
    to: 'Belgium',
    importValue: 450,
    exportValue: 1200,
    coords: [50.6, 6.2] as L.LatLngExpression,
  },
  {
    from: 'Germany',
    to: 'Netherlands',
    importValue: 1100,
    exportValue: 1200,
    coords: [52.1, 6.9] as L.LatLngExpression,
  },
];
