export type AnalyticsTimespan = '1h' | '6h' | '24h' | '7d';

export interface GridLoadPoint {
  readonly timestamp: string;
  readonly consumptionKwh: number;
}

export interface GridLoadAnalytics {
  readonly timespan: AnalyticsTimespan;
  readonly from: string;
  readonly to: string;
  readonly totalConsumptionKwh: number;
  readonly averageConsumptionKwh: number;
  readonly peakConsumptionKwh: number;
  readonly readingCount: number;
  readonly activeMeterCount: number;
  readonly loadProfile: readonly GridLoadPoint[];
}

export interface IngestReadingRequest {
  readonly meterId: string;
  readonly gridArea: string;
  readonly consumptionKwh: number;
  readonly recordedAt: string;
}

export interface IngestReadingResponse extends IngestReadingRequest {
  readonly receivedAt: string;
}

export interface ApiProblem {
  readonly title: string;
  readonly detail: string;
  readonly status: number;
  readonly violations?: Readonly<Record<string, string>>;
}

export type MeterStatus = 'ACTIVE' | 'INACTIVE';
export type OperationalState = 'NORMAL' | 'ELEVATED' | 'CRITICAL';

export interface MeterSummary {
  readonly meterId: string;
  readonly gridArea: string;
  readonly status: MeterStatus;
  readonly createdAt: string;
  readonly readingCount: number;
  readonly lastReadingAt: string | null;
}

export interface GridAreaLoad {
  readonly gridArea: string;
  readonly totalConsumptionKwh: number;
  readonly averageConsumptionKwh: number;
  readonly peakConsumptionKwh: number;
  readonly readingCount: number;
  readonly activeMeterCount: number;
  readonly loadSharePercent: number;
  readonly operationalState: OperationalState;
}

export interface GridAreaAnalytics {
  readonly timespan: AnalyticsTimespan;
  readonly from: string;
  readonly to: string;
  readonly totalConsumptionKwh: number;
  readonly areas: readonly GridAreaLoad[];
}

export interface LoadAnomaly {
  readonly meterId: string;
  readonly gridArea: string;
  readonly recordedAt: string;
  readonly consumptionKwh: number;
  readonly thresholdKwh: number;
  readonly excessPercent: number;
}
