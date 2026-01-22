import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { API } from './api.config';
import { Attendant } from '../models/attendant.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AttendantApi {
  constructor(private http: HttpClient) {}

  list(team?: string): Observable<Attendant[]> {
    const q = team ? `?team=${team}` : '';
    return this.http.get<Attendant[]>(`${API.baseUrl}/attendants${q}`);
  }

  toggleActive(id: number, active: boolean): Observable<Attendant> {
    return this.http.patch<Attendant>(`${API.baseUrl}/attendants/${id}/toggle-active`, { active });
  }
}
