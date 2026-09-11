// Stage 4 — Angular 22 feature screens
// Reactive form matching contract §4 CreateTicketRequest (title*, description?, category?,
// priority*, assigneeId?, dueAt?, links?[]). Validation messages are wired to read from a
// `serverFieldErrors` signal shaped like the contract's error envelope `fieldErrors` map,
// so once POST /api/v1/tickets is wired the same template renders backend validation too.

import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import type { CreateTicketRequest, LinkPlatform, Priority, UserSummary } from '@core/models';
import { MATERIAL_IMPORTS } from '@shared/material';

type LinkGroup = FormGroup<{
  url: FormControl<string>;
  linkTitle: FormControl<string>;
  platform: FormControl<LinkPlatform | ''>;
}>;

const PRIORITIES: Priority[] = ['Critical', 'High', 'Medium', 'Low'];
const PLATFORM_OPTIONS: LinkPlatform[] = ['keka', 'google-drive', 'google-docs', 'github', 'slack', 'other'];

// TODO(stage 4): replace with a real assignee list via UserApiService.listUsers().
const MOCK_USERS: UserSummary[] = [
  { id: 'u-1', fullName: 'Ada Lovelace', email: 'ada@syncboard.dev', department: 'Platform', active: true },
  { id: 'u-2', fullName: 'Grace Hopper', email: 'grace@syncboard.dev', department: 'Platform', active: true },
  { id: 'u-3', fullName: 'Alan Turing', email: 'alan@syncboard.dev', department: 'Infra', active: true },
];

@Component({
  selector: 'sb-ticket-create-page',
  standalone: true,
  imports: [ReactiveFormsModule, ...MATERIAL_IMPORTS],
  templateUrl: './ticket-create-page.component.html',
  styleUrl: './ticket-create-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TicketCreatePageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  protected readonly priorities = PRIORITIES;
  protected readonly platformOptions = PLATFORM_OPTIONS;
  protected readonly availableAssignees = MOCK_USERS;

  protected readonly submitting = signal(false);
  protected readonly serverFieldErrors = signal<Record<string, string> | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    title: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(200)]),
    description: this.fb.nonNullable.control(''),
    category: this.fb.nonNullable.control(''),
    priority: this.fb.nonNullable.control<Priority>('Medium', Validators.required),
    assigneeId: this.fb.control<string | null>(null),
    dueAt: this.fb.control<Date | null>(null),
    links: this.fb.array<LinkGroup>([]),
  });

  protected get linksArray(): FormArray<LinkGroup> {
    return this.form.controls.links;
  }

  protected addLink(): void {
    this.linksArray.push(this.buildLinkGroup());
  }

  protected removeLinkAt(index: number): void {
    this.linksArray.removeAt(index);
  }

  protected fieldError(field: string): string | null {
    return this.serverFieldErrors()?.[field] ?? null;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const request: CreateTicketRequest = {
      title: value.title.trim(),
      description: value.description.trim() || undefined,
      category: value.category.trim() || undefined,
      priority: value.priority,
      assigneeId: value.assigneeId || undefined,
      dueAt: value.dueAt ? value.dueAt.toISOString() : undefined,
      links: value.links
        .filter((link) => link.url.trim().length > 0)
        .map((link) => ({
          url: link.url.trim(),
          linkTitle: link.linkTitle.trim() || undefined,
          platform: link.platform || undefined,
        })),
    };

    // TODO(stage 4): POST via TicketApiService.createTicket(request), then
    // router.navigate(['/tickets', created.id]). On a 400 VALIDATION_FAILED response,
    // set serverFieldErrors(error.fieldErrors) to surface messages under each control.
    this.submitting.set(true);
    setTimeout(() => {
      this.submitting.set(false);
      void this.router.navigateByUrl('/board');
    }, 300);
  }

  protected cancel(): void {
    void this.router.navigateByUrl('/board');
  }

  private buildLinkGroup(): LinkGroup {
    return this.fb.nonNullable.group({
      url: this.fb.nonNullable.control('', Validators.required),
      linkTitle: this.fb.nonNullable.control(''),
      platform: this.fb.nonNullable.control<LinkPlatform | ''>(''),
    });
  }
}
