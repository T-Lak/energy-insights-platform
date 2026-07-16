export const COUNTRY_NAMES: Record<string, string> = {
  AT: 'Austria',
  BE: 'Belgium',
  CH: 'Switzerland',
  CZ: 'Czech Republic',
  DE_LU: 'Germany',
  DK: 'Denmark',
  FR: 'France',
  NL: 'Netherlands',
  NO: 'Norway',
  PL: 'Poland',
  SE: 'Sweden',
};

export function getCountryFullName(abbreviation: string): string {
  if (!abbreviation) return '';

  return COUNTRY_NAMES[abbreviation.toUpperCase()] || abbreviation;
}

export function getCountryCodes(excludeCode?: string) {
  return Object.keys(COUNTRY_NAMES).filter((c) => c !== excludeCode);
}
