import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EMPTY, catchError, finalize } from 'rxjs';
import { ApiProblem, IngestReadingResponse } from '../core/api.models';
import { SmartGridApiService } from '../core/smart-grid-api.service';

@Component({
  selector: 'app-reading-ingest',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <section class="ingest-card" aria-labelledby="ingest-heading">
      <div class="form-intro">
        <p class="eyebrow">Telemetry intake</p>
        <h2 id="ingest-heading">Ingest a meter reading</h2>
        <p>Validate the ingestion flow with an ISO-timestamped consumption signal.</p>
      </div>
      <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
        <label>Meter ID
          <input formControlName="meterId" autocomplete="off" placeholder="AT-VIE-1042" />
          @if (invalid('meterId')) { <small>Use 3–64 letters, numbers or hyphens.</small> }
        </label>
        <label>Grid area
          <input formControlName="gridArea" autocomplete="off" placeholder="Vienna-Center" />
          @if (invalid('gridArea')) { <small>Grid area is required.</small> }
        </label>
        <label>Consumption (kWh)
          <input formControlName="consumptionKwh" type="number" min="0" max="999999" step="0.0001" />
          @if (invalid('consumptionKwh')) { <small>Enter a non-negative consumption value.</small> }
        </label>
        <label>Recorded at
          <input formControlName="recordedAt" type="datetime-local" />
          @if (invalid('recordedAt')) { <small>A timestamp is required.</small> }
        </label>
        <div class="form-actions">
          <button type="submit" [disabled]="submitting()">
            @if (submitting()) { Sending… } @else { Ingest reading <span>↗</span> }
          </button>
          @if (success()) { <p class="feedback success" role="status">Reading accepted for {{ success() }}</p> }
          @if (error()) { <p class="feedback error" role="alert">{{ error() }}</p> }
        </div>
      </form>
    </section>
  `,
  styleUrl: './reading-ingest.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReadingIngestComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly api = inject(SmartGridApiService);
  readonly readingCreated = output<IngestReadingResponse>();
  readonly submitting = signal(false);
  readonly success = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly form = this.formBuilder.nonNullable.group({
    meterId: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9-]{3,64}$/)]],
    gridArea: ['', [Validators.required, Validators.maxLength(80)]],
    consumptionKwh: [0, [Validators.required, Validators.min(0), Validators.max(999999)]],
    recordedAt: [this.localDateTime(), Validators.required]
  });

  invalid(name: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[name];
    return control.invalid && (control.dirty || control.touched);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.success.set(null);
    this.error.set(null);
    const value = this.form.getRawValue();
    this.api.ingestReading({ ...value, recordedAt: new Date(value.recordedAt).toISOString() }).pipe(
      catchError((response: HttpErrorResponse) => {
        const problem = response.error as Partial<ApiProblem> | null;
        this.error.set(problem?.detail ?? 'The reading could not be ingested. Try again.');
        return EMPTY;
      }),
      finalize(() => this.submitting.set(false))
    ).subscribe(response => {
      this.success.set(response.meterId);
      this.readingCreated.emit(response);
      this.form.controls.consumptionKwh.reset(0);
      this.form.controls.recordedAt.reset(this.localDateTime());
    });
  }

  private localDateTime(): string {
    const date = new Date();
    date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    return date.toISOString().slice(0, 16);
  }
}

