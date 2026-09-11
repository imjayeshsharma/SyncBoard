// Stage 4 — Angular 22 feature screens
// Wildcard ('**') route target.

import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MATERIAL_IMPORTS } from '@shared/material';

@Component({
  selector: 'sb-not-found-page',
  standalone: true,
  imports: [RouterLink, ...MATERIAL_IMPORTS],
  templateUrl: './not-found-page.component.html',
  styleUrl: './not-found-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotFoundPageComponent {}
