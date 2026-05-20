import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-subpage-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './subpage-header.html',
  styleUrl: './subpage-header.scss',
})
export class SubpageHeader {
  @Input() title!: string;
  @Input() icon!: string;
}
