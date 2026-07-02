export interface RenewableMixDTO {
  timestamp: string;
  solar: number;
  windOnshore: number;
  windOffshore: number;
  biomass: number;
  hydro: number;
  geothermal: number;
  otherRenewable: number;
}
