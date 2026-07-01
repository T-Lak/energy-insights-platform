import { ColDef } from 'ag-grid-community';
import { getCountryFullName } from '../../../core/model/domain/country.model';
import {
  FlowDirection,
  getFlowBackgroundColor,
  getFlowColor,
} from '../../../core/model/domain/flows.model';

export function getColumnDefs(granularity: string): ColDef[] {
  const baseDefs: ColDef[] = [
    {
      field: 'country',
      headerName: 'Country',
      valueFormatter: (params: any) => {
        if (!params.value) return '';
        const countryName = getCountryFullName(params.value);
        return params.value;
      },
      tooltipValueGetter: (params: any) => {
        if (!params.value) return '';
        const countryName = getCountryFullName(params.value);
        return countryName;
      },
    },
    {
      field: 'flowDirection',
      headerName: 'Flow Direction',
      filter: true,
      cellRenderer: (params: any) => {
        if (!params.value) return '';
        const isImport = params.value.toUpperCase() === 'IMPORT';
        const backgroundColor = isImport
          ? getFlowBackgroundColor(FlowDirection.Import)
          : getFlowBackgroundColor(FlowDirection.Export);
        const textColor = isImport
          ? getFlowColor(FlowDirection.Import)
          : getFlowColor(FlowDirection.Export);

        return `
      <span style="
        background-color: ${backgroundColor}; 
        color: ${textColor}; 
        padding: 4px 10px; 
        border-radius: 12px; 
        font-size: 12px; 
        font-weight: 600;
        display: inline-block;
        line-height: 1.2;
      ">
        ${params.value}
      </span>
    `;
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
    { field: 'value', headerName: 'Flow (MW)' },
    {
      field: 'netFlow',
      headerName: 'Net Flow (MW)',
      valueFormatter: (params) => (params.value != null ? params.value.toFixed(2) : '-'),
      cellStyle: (params) => {
        if (params.value > 0) {
          return { color: '#205e23', fontWeight: '500' };
        } else if (params.value < 0) {
          return { color: '#9c1a1a', fontWeight: '500' };
        }
        return null;
      },
    },
  ];

  if (granularity === 'weekly') {
    return [
      ...baseDefs,
      {
        field: 'shortTermChange',
        headerName: '1d Change (%)',
        valueFormatter: (params) => (params.value != null ? `${params.value.toFixed(1)}%` : '0.0%'),
      },
      {
        field: 'longTermChange',
        headerName: '7d Change (%)',
        valueFormatter: (params) => (params.value != null ? `${params.value.toFixed(1)}%` : '0.0%'),
      },
    ];
  } else if (granularity === 'monthly') {
    return [
      ...baseDefs,
      {
        field: 'shortTermChange',
        headerName: '7d Change (%)',
        valueFormatter: (params) => (params.value != null ? `${params.value.toFixed(1)}%` : '0.0%'),
      },
      {
        field: 'longTermChange',
        headerName: '30d Change (%)',
        valueFormatter: (params) => (params.value != null ? `${params.value.toFixed(1)}%` : '0.0%'),
      },
    ];
  } else {
    return [
      ...baseDefs,
      {
        field: 'shortTermChange',
        headerName: '1h Change (%)',
        valueFormatter: (params) => (params.value != null ? `${params.value.toFixed(1)}%` : '0.0%'),
      },
      {
        field: 'longTermChange',
        headerName: '24h Change (%)',
        valueFormatter: (params) => (params.value != null ? `${params.value.toFixed(1)}%` : '0.0%'),
      },
    ];
  }
}
