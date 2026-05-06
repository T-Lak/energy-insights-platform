import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class WebsocketService {
  private socket!: WebSocket;
  private messageSubject = new Subject<any>();

  messages$ = this.messageSubject.asObservable();

  connect(): void {
    this.socket = new WebSocket('ws://localhost:8080/ws');

    this.socket.onmessage = (event) => {
      this.messageSubject.next(JSON.parse(event.data));
    };
  }
}
