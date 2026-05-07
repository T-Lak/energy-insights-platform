import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class WebsocketService {
  private stompClient!: Client;

  private messageSubject = new Subject<any>();

  messages$ = this.messageSubject.asObservable();

  constructor() {
    this.connect();
  }

  connect(): void {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-analytics'),

      reconnectDelay: 5000,

      onConnect: () => {
        console.log('Connected to websocket');

        this.stompClient.subscribe('/topic/grid_metrics', (message: IMessage) => {
          const body = JSON.parse(message.body);

          this.messageSubject.next(body);
        });
      },

      onStompError: (frame) => {
        console.error('Broker error:', frame);
      },
    });

    this.stompClient.activate();
  }
}
