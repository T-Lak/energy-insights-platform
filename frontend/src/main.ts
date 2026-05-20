import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

import { ModuleRegistry } from 'ag-grid-community';
import { AllCommunityModule } from 'ag-grid-community';

bootstrapApplication(App, appConfig).catch((err) => console.error(err));

ModuleRegistry.registerModules([AllCommunityModule]);
