export interface CrossborderFlowSummaryDTO {
  timestamp: string;
  country: string;
  totalImportMW: number;
  totalExportMW: number;
  netFlow: number;
  importShortTermChangePercentage: number;
  exportShortTermChangePercentage: number;
  importLongTermChangePercentage: number;
  exportLongTermChangePercentage: number;
}
