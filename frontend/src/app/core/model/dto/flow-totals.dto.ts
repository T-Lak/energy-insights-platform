export interface FlowTotalsDTO {
  timestamp: string;
  region: string;
  totalExportMW: number;
  totalImportMW: number;
  netExchangeMW: number;
}
