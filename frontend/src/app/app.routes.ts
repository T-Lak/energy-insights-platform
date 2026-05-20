import { Routes } from '@angular/router';

import { Dashboard } from './features/dashboard/dashboard';
import { RenewableShare } from './features/renewable-share/renewable-share';
import { CrossborderFlows } from './features/crossborder-flows/crossborder-flows';

export const routes: Routes = [
  {
    path: '',
    component: Dashboard,
  },
  {
    path: 'analytics',
    children: [
      {
        path: 'renewable-share',
        component: RenewableShare,
      },
      {
        path: 'crossborder-flows',
        component: CrossborderFlows,
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
