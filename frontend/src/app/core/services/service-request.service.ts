import { Injectable } from '@angular/core';
import { ServiceRequestApi } from '../api/service-request.api';
import { Observable } from 'rxjs';
import { ServiceRequest } from '../models/service-request.model';

@Injectable({ providedIn: 'root' })
export class ServiceRequestService {
  constructor(private api: ServiceRequestApi) {}

  create(customerName: string, subject: string): Observable<ServiceRequest> {
    return this.api.create(customerName, subject);
  }

  list(team?: string, status?: string): Observable<ServiceRequest[]> {
    return this.api.list(team, status);
  }

  finish(id: number): Observable<ServiceRequest> {
    return this.api.finish(id);
  }
}
