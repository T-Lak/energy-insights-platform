export const SOURCE_DISPLAY_NAMES: Record<string, string> = {
  'fossil brown coal/lignite': 'Lignite',
  'fossil hard coal': 'Hard Coal',
  'fossil gas': 'Gas',
  'fossil oil': 'Oil',
  'hydro run-of-river and poundage': 'Hydro (River)',
  'hydro pumped storage': 'Hydro (Pumped)',
  'wind onshore': 'Wind Onshore',
  'wind offshore': 'Wind Offshore',
  solar: 'Solar',
  nuclear: 'Nuclear',
  biomass: 'Biomass',
};

export function shortenSourceName(name: string): string {
  if (!name) return '';
  const lowerName = name.trim().toLowerCase();
  return SOURCE_DISPLAY_NAMES[lowerName] || name;
}
