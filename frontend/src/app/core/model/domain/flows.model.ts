export enum FlowDirection {
  Export,
  Import,
}

export function getFlowColor(direction: FlowDirection): string {
  switch (direction) {
    case FlowDirection.Export:
      return '#4a148c';
    case FlowDirection.Import:
      return '#0d47a1';
    default:
      return '#535353';
  }
}

export function getFlowBackgroundColor(direction: FlowDirection): string {
  switch (direction) {
    case FlowDirection.Export:
      return '#f3e5f5';
    case FlowDirection.Import:
      return '#e3f2fd';
    default:
      return '#f0f0f0';
  }
}
