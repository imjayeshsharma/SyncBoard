// Stage 4 — Angular 22 shell

import type { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'board' },
  {
    path: 'board',
    loadComponent: () =>
      import('@features/board/board-page.component').then((m) => m.BoardPageComponent),
    title: 'Board · SyncBoard',
  },
  {
    path: 'my-tasks',
    loadComponent: () =>
      import('@features/my-tasks/my-tasks-page.component').then((m) => m.MyTasksPageComponent),
    title: 'My Tasks · SyncBoard',
  },
  {
    path: 'tickets/new',
    loadComponent: () =>
      import('@features/ticket-create/ticket-create-page.component').then(
        (m) => m.TicketCreatePageComponent,
      ),
    title: 'New Ticket · SyncBoard',
  },
  {
    path: 'tickets/:id',
    loadComponent: () =>
      import('@features/ticket-detail/ticket-detail-page.component').then(
        (m) => m.TicketDetailPageComponent,
      ),
    title: 'Ticket · SyncBoard',
  },
  {
    path: '**',
    loadComponent: () =>
      import('@features/not-found/not-found-page.component').then(
        (m) => m.NotFoundPageComponent,
      ),
    title: 'Not Found · SyncBoard',
  },
];
