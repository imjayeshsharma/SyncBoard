// Stage 4 — Angular 22 feature screens
// Smoke test: BoardStore/AuthStore come from A5's core layer via DI, so we let the real
// (skeleton) providers construct and only assert the toolbar renders. Deeper interaction
// tests (filtering, drop handling) are stubbed as xit() until BoardStore's data-fetching
// bodies are implemented past TODO(stage 4).

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { BoardPageComponent } from './board-page.component';

describe('BoardPageComponent', () => {
  let fixture: ComponentFixture<BoardPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BoardPageComponent],
      providers: [provideZonelessChangeDetection(), provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()],
    }).compileComponents();

    fixture = TestBed.createComponent(BoardPageComponent);
  });

  it('renders the toolbar and all six board columns in contract order, even before data loads', () => {
    fixture.detectChanges();
    const search = fixture.nativeElement.querySelector('input[placeholder="Search tickets…"]');
    const columns: NodeListOf<Element> = fixture.nativeElement.querySelectorAll('sb-board-column');
    expect(search).toBeTruthy();
    // toOrderedColumns() (shared/transitions.ts) always yields 6 entries regardless of
    // BoardStore's load state, so this holds even while BoardStore.load() is still TODO.
    expect(columns.length).toBe(6);
  });

  // eslint-disable-next-line @typescript-eslint/no-empty-function
  xit('filters columns by the search term', () => {});
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  xit('filters columns by priority', () => {});
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  xit('filters columns by "my tickets only"', () => {});
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  xit('computes the live overdue count across filtered columns', () => {});
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  xit('calls BoardStore.moveTicket with the target column status on drop', () => {});
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  xit('opens the reason dialog before moving a card to Blocked or Cancelled', () => {});
});
