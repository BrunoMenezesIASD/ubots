import { Injectable } from '@angular/core';
import { BehaviorSubject, filter } from 'rxjs';
import { DashboardApi } from '../api/dashboard.api';
import { StreamApi } from '../api/stream.api';
import { DashboardSummary } from '../models/dashboard.model';
import { StreamEvent } from '../models/stream-event.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly _summary$ = new BehaviorSubject<DashboardSummary | null>(null);
  readonly summary$ = this._summary$.asObservable().pipe(filter((v): v is DashboardSummary => v !== null));

  constructor(private api: DashboardApi, private streamApi: StreamApi) {}

  load() {
    this.api.getSummary().subscribe(v => this._summary$.next(v));
  }

  connectStream() {
    return this.streamApi.connect().subscribe({
      next: (evt: StreamEvent) => {
        if (evt.type === 'DASHBOARD_UPDATED' && evt.payload) {
          this._summary$.next(evt.payload as DashboardSummary);
        }
      },
      error: () => {
        // stream caiu; poderia implementar retry
      }
    });
  }
}
