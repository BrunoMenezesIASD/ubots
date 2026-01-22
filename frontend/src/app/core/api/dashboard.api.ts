import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { API } from './api.config';
import { DashboardSummary } from '../models/dashboard.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DashboardApi {
  constructor(private http: HttpClient) {}

  getSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${API.baseUrl}/dashboard/summary`);
  }
}
