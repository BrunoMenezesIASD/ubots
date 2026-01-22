import { Injectable } from '@angular/core';
import { StreamApi } from '../api/stream.api';

@Injectable({ providedIn: 'root' })
export class StreamService {
  constructor(private api: StreamApi) {}
  connect() { return this.api.connect(); }
}
