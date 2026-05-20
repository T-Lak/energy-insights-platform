import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-widget',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './widget.html',
  styleUrl: './widget.scss',
})
export class Widget {
  @Input() title!: string;
  @Input() icon!: string;
}
