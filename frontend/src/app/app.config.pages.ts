import { provideHttpClient } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { DemoSmartGridApiService } from './core/demo-smart-grid-api.service';
import { SmartGridApiService } from './core/smart-grid-api.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(),
    { provide: SmartGridApiService, useClass: DemoSmartGridApiService }
  ]
};
