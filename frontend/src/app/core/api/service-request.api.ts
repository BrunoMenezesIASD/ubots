import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { API } from './api.config';
import { ServiceRequest } from '../models/service-request.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ServiceRequestApi {
  constructor(private http: HttpClient) {}

  create(customerName: string, subject: string): Observable<ServiceRequest> {
    return this.http.post<ServiceRequest>(`${API.baseUrl}/requests`, { customerName, subject });
  }

  list(team?: string, status?: string): Observable<ServiceRequest[]> {
    const params: string[] = [];
    if (team) params.push(`team=${team}`);
    if (status) params.push(`status=${status}`);
    const q = params.length ? `?${params.join('&')}` : '';
    return this.http.get<ServiceRequest[]>(`${API.baseUrl}/requests${q}`);
  }

  finish(id: number): Observable<ServiceRequest> {
    return this.http.post<ServiceRequest>(`${API.baseUrl}/requests/${id}/finish`, {});
  }
}
