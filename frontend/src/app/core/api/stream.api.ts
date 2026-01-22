import { Injectable } from '@angular/core';
import { API } from './api.config';
import { Observable } from 'rxjs';
import { StreamEvent } from '../models/stream-event.model';

@Injectable({ providedIn: 'root' })
export class StreamApi {
  connect(): Observable<StreamEvent> {
    return new Observable<StreamEvent>((subscriber) => {
      const es = new EventSource(API.streamUrl);

      es.onmessage = (msg) => {
        // fallback (if server sends unnamed events)
        try { subscriber.next(JSON.parse(msg.data)); } catch {}
      };

      const types = [
        'REQUEST_CREATED','REQUEST_QUEUED','REQUEST_ASSIGNED','REQUEST_FINISHED','ATTENDANT_UPDATED','DASHBOARD_UPDATED'
      ];
      types.forEach(t => es.addEventListener(t, (e: MessageEvent) => {
        try { subscriber.next(JSON.parse(e.data)); } catch {}
      }));

      es.onerror = (err) => {
        subscriber.error(err);
        es.close();
      };

      return () => es.close();
    });
  }
}
