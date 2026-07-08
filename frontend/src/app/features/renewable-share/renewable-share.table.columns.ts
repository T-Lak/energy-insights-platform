import { ColDef } from 'ag-grid-community';
import { getSourceDisplayName } from '../../core/model/domain/sources.model';
import { getNumericTrendClass } from '../../core/utils/grid.utils';

export function getColumnDefs(): ColDef[] {
  const baseDefs: ColDef[] = [
    {
      field: 'source',
      headerName: 'Source',
      valueFormatter: (params: any) => {
        return getSourceDisplayName(params.value);
      },
    },
    {
      field: 'timestamp',
      headerName: 'Date/Time',
      valueFormatter: (params) => {
        if (!params.value) return '-';
        const date = new Date(params.value);

        return date.toLocaleString([], {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
          hour12: false,
        });
      },
    },
    {
      field: 'avgGenerationMW',
      headerName: 'Average Generation (MW)',
      valueFormatter: (params) => {
        if (params.value == null) return '0.0';
        return new Intl.NumberFormat([], {
          minimumFractionDigits: 1,
          maximumFractionDigits: 1,
        }).format(params.value);
      },
    },
    {
      field: 'change1hPercentage',
      headerName: '1h Change (%)',
      valueFormatter: (params) => (params.value != null ? `${params.value.toFixed(1)}` : '0.0'),
      cellClass: getNumericTrendClass,
    },
    {
      field: 'change24hPercentage',
      headerName: '24h Change (%)',
      valueFormatter: (params) => (params.value != null ? `${params.value.toFixed(1)}` : '0.0'),
      cellClass: getNumericTrendClass,
    },
  ];

  return baseDefs;
}
