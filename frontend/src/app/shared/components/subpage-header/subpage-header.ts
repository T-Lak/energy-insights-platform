import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { map, Observable, switchMap, timer } from 'rxjs';
import { WebsocketService } from '../../../core/services/websocket.service';

@Component({
  selector: 'app-subpage-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './subpage-header.html',
  styleUrl: './subpage-header.scss',
})
export class SubpageHeader implements OnInit {
  @Input() title!: string;
  @Input() icon!: string;

  protected lastUpdateAgo$!: Observable<string>;

  constructor(private websocketService: WebsocketService) {}

  ngOnInit(): void {
    this.lastUpdateAgo$ = this.websocketService.lastUpdate$.pipe(
      switchMap((lastDate) =>
        timer(0, 60000).pipe(
          map(() => {
            const diffMins = Math.floor((new Date().getTime() - lastDate.getTime()) / 60000);
            return diffMins < 1 ? 'Just now' : `${diffMins} min${diffMins > 1 ? 's' : ''} ago`;
          }),
        ),
      ),
    );
  }
}
