import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { SubpageHeader } from '@shared/components/subpage-header/subpage-header';

@Component({
  selector: 'app-renewable-share',
  standalone: true,
  imports: [CommonModule, SubpageHeader],
  templateUrl: './renewable-share.html',
  styleUrl: './renewable-share.scss',
})
export class RenewableShare {}
