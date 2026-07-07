export enum SourceType {
  Fossil = 'Fossil',
  Renewable = 'Renewable',
  Other = 'Other',
}

export enum EnergySource {
  BIOMASS = 'BIOMASS',
  FOSSIL_BROWN_COAL = 'FOSSIL_BROWN_COAL',
  FOSSIL_COAL_DERIVED_GAS = 'FOSSIL_COAL_DERIVED_GAS',
  FOSSIL_GAS = 'FOSSIL_GAS',
  FOSSIL_HARD_COAL = 'FOSSIL_HARD_COAL',
  FOSSIL_OIL = 'FOSSIL_OIL',
  GEOTHERMAL = 'GEOTHERMAL',
  HYDRO_PUMPED_STORAGE = 'HYDRO_PUMPED_STORAGE',
  HYDRO_RUN_OF_RIVER = 'HYDRO_RUN_OF_RIVER',
  HYDRO_WATER_RESERVOIR = 'HYDRO_WATER_RESERVOIR',
  OTHER = 'OTHER',
  OTHER_RENEWABLE = 'OTHER_RENEWABLE',
  SOLAR = 'SOLAR',
  WASTE = 'WASTE',
  WIND_OFFSHORE = 'WIND_OFFSHORE',
  WIND_ONSHORE = 'WIND_ONSHORE',
}

interface EnergySourceMetadata {
  displayName: string;
  type: SourceType;
  isRenewable: boolean;
}

export const ENERGY_SOURCE_MAP: Record<EnergySource, EnergySourceMetadata> = {
  [EnergySource.BIOMASS]: { displayName: 'Biomass', type: SourceType.Renewable, isRenewable: true },
  [EnergySource.GEOTHERMAL]: {
    displayName: 'Geothermal',
    type: SourceType.Renewable,
    isRenewable: true,
  },
  [EnergySource.HYDRO_RUN_OF_RIVER]: {
    displayName: 'Hydro (River)',
    type: SourceType.Renewable,
    isRenewable: true,
  },
  [EnergySource.HYDRO_WATER_RESERVOIR]: {
    displayName: 'Hydro (Reservoir)',
    type: SourceType.Renewable,
    isRenewable: true,
  },
  [EnergySource.OTHER_RENEWABLE]: {
    displayName: 'Other Renewable',
    type: SourceType.Renewable,
    isRenewable: true,
  },
  [EnergySource.SOLAR]: { displayName: 'Solar', type: SourceType.Renewable, isRenewable: true },
  [EnergySource.WIND_OFFSHORE]: {
    displayName: 'Wind Offshore',
    type: SourceType.Renewable,
    isRenewable: true,
  },
  [EnergySource.WIND_ONSHORE]: {
    displayName: 'Wind Onshore',
    type: SourceType.Renewable,
    isRenewable: true,
  },

  [EnergySource.FOSSIL_BROWN_COAL]: {
    displayName: 'Lignite',
    type: SourceType.Fossil,
    isRenewable: false,
  },
  [EnergySource.FOSSIL_HARD_COAL]: {
    displayName: 'Hard Coal',
    type: SourceType.Fossil,
    isRenewable: false,
  },
  [EnergySource.FOSSIL_GAS]: { displayName: 'Gas', type: SourceType.Fossil, isRenewable: false },
  [EnergySource.FOSSIL_OIL]: { displayName: 'Oil', type: SourceType.Fossil, isRenewable: false },
  [EnergySource.FOSSIL_COAL_DERIVED_GAS]: {
    displayName: 'Coal Gas',
    type: SourceType.Fossil,
    isRenewable: false,
  },

  [EnergySource.HYDRO_PUMPED_STORAGE]: {
    displayName: 'Hydro (Pumped)',
    type: SourceType.Other,
    isRenewable: false,
  },
  [EnergySource.WASTE]: { displayName: 'Waste', type: SourceType.Other, isRenewable: false },
  [EnergySource.OTHER]: { displayName: 'Other', type: SourceType.Other, isRenewable: false },
};

export function parseEnergySource(rawName: string): EnergySource {
  if (!rawName) return EnergySource.OTHER;

  const lookup: Record<string, EnergySource> = {
    'fossil brown coal/lignite': EnergySource.FOSSIL_BROWN_COAL,
    'fossil hard coal': EnergySource.FOSSIL_HARD_COAL,
    'fossil gas': EnergySource.FOSSIL_GAS,
    'fossil oil': EnergySource.FOSSIL_OIL,
    'fossil coal-derived gas': EnergySource.FOSSIL_COAL_DERIVED_GAS,
    'hydro run-of-river and poundage': EnergySource.HYDRO_RUN_OF_RIVER,
    'hydro pumped storage': EnergySource.HYDRO_PUMPED_STORAGE,
    'hydro water reservoir': EnergySource.HYDRO_WATER_RESERVOIR,
    'wind onshore': EnergySource.WIND_ONSHORE,
    'wind offshore': EnergySource.WIND_OFFSHORE,
    solar: EnergySource.SOLAR,
    nuclear: EnergySource.OTHER,
    biomass: EnergySource.BIOMASS,
    waste: EnergySource.WASTE,
    geothermal: EnergySource.GEOTHERMAL,
  };

  const key = rawName.trim().toLowerCase();
  return (
    lookup[key] ||
    (rawName.toUpperCase() in EnergySource
      ? (rawName.toUpperCase() as EnergySource)
      : EnergySource.OTHER)
  );
}

export function shortenSourceName(source: EnergySource | string): string {
  if (!source) return '';
  const enumKey =
    source in ENERGY_SOURCE_MAP ? (source as EnergySource) : parseEnergySource(source);
  return ENERGY_SOURCE_MAP[enumKey]?.displayName || source;
}

export function getSourceType(source: EnergySource | string): SourceType {
  if (!source) return SourceType.Other;
  const enumKey =
    source in ENERGY_SOURCE_MAP ? (source as EnergySource) : parseEnergySource(source);
  return ENERGY_SOURCE_MAP[enumKey]?.type || SourceType.Other;
}

export function getSourceTypeColor(source: EnergySource | string): string {
  const type = getSourceType(source);
  switch (type) {
    case SourceType.Renewable:
      return '#0f766e';
    case SourceType.Fossil:
      return '#2F3542';
    default:
      return '#535353';
  }
}

export function getRenewablesDisplayNames() {
  return Object.values(ENERGY_SOURCE_MAP)
    .filter((source) => source.type === SourceType.Renewable)
    .map((source) => source.displayName);
}
