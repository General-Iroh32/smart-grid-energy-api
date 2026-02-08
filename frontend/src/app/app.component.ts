import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <header class="topbar">
      <a class="brand" href="#main" aria-label="Smart Grid Operations home">
        <span class="brand-mark" aria-hidden="true">SG</span>
        <span><strong>Smart Grid</strong><small>Operations Console</small></span>
      </a>
      <div class="environment"><span></span> Demo environment</div>
    </header>
    <main id="main" class="page-shell">
      <section class="hero">
        <p class="eyebrow">Energy intelligence · Vienna</p>
        <h1>Grid signals,<br /><span>made actionable.</span></h1>
        <p>Monitor consumption, identify demand peaks and validate incoming smart-meter telemetry.</p>
      </section>
    </main>
  `,
  styleUrl: './app.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent {}

