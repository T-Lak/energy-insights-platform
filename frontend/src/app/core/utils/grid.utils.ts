import { CellClassParams } from 'ag-grid-community';

export const getNumericTrendClass = (params: CellClassParams): string | null => {
  const value = params.value;
  if (value == null || value === 0) return null;
  return value > 0 ? 'grid-cell-positive' : 'grid-cell-negative';
};
