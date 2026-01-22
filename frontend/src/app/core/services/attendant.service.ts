import { Injectable } from '@angular/core';
import { AttendantApi } from '../api/attendant.api';
import { Observable } from 'rxjs';
import { Attendant } from '../models/attendant.model';

@Injectable({ providedIn: 'root' })
export class AttendantService {
  constructor(private api: AttendantApi) {}

  list(team?: string): Observable<Attendant[]> {
    return this.api.list(team);
  }

  toggleActive(id: number, active: boolean): Observable<Attendant> {
    return this.api.toggleActive(id, active);
  }
}
