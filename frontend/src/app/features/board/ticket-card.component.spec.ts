// Stage 4 — Angular 22 feature screens

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import type { TicketSummary } from '@core/models';
import { TicketCardComponent } from './ticket-card.component';

const MOCK_TICKET: TicketSummary = {
  id: 't-1',
  title: 'Fix the flaky deploy pipeline',
  status: 'InProgress',
  priority: 'High',
  category: 'Infra',
  assignee: { id: 'u-1', fullName: 'Ada Lovelace', email: 'ada@syncboard.dev', active: true },
  reporter: { id: 'u-2', fullName: 'Grace Hopper', email: 'grace@syncboard.dev', active: true },
  dueAt: '2026-09-20T00:00:00Z',
  overdue: false,
  commentCount: 2,
  linkCount: 1,
  createdAt: '2026-09-01T00:00:00Z',
  updatedAt: '2026-09-02T00:00:00Z',
  version: 0,
};

describe('TicketCardComponent', () => {
  let fixture: ComponentFixture<TicketCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketCardComponent],
      providers: [provideZonelessChangeDetection(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(TicketCardComponent);
    fixture.componentRef.setInput('ticket', MOCK_TICKET);
  });

  it('renders the ticket title', () => {
    fixture.detectChanges();
    const title = fixture.nativeElement.querySelector('.sb-ticket-card__title');
    expect(title?.textContent).toContain('Fix the flaky deploy pipeline');
  });

  // eslint-disable-next-line @typescript-eslint/no-empty-function
  xit('shows the overdue badge when ticket.overdue is true', () => {});
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  xit('links to /tickets/:id', () => {});
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  xit('renders "Unassigned" when there is no assignee', () => {});
});
