import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { EmployeeService } from '../../core/employee.service';
import { DEPARTMENTS, Employee, EmployeeInput, STATUS_LABEL } from '../../core/models';

function todayIso(): string {
  const d = new Date();
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
}

function notFuture(c: AbstractControl): ValidationErrors | null {
  return c.value && c.value > todayIso() ? { future: true } : null;
}

@Component({
  selector: 'app-employee-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  template: `
    <h2 mat-dialog-title>{{ data ? 'Edit employee' : 'Add employee' }}</h2>
    <form [formGroup]="form" (ngSubmit)="save()" novalidate>
      <mat-dialog-content>
        <div class="grid">
          <mat-form-field appearance="outline">
            <mat-label>First name</mat-label>
            <input matInput formControlName="firstName" />
            <mat-error>{{ err('firstName') }}</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Last name</mat-label>
            <input matInput formControlName="lastName" />
            <mat-error>{{ err('lastName') }}</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="wide">
            <mat-label>Email</mat-label>
            <input matInput type="email" formControlName="email" autocomplete="off" />
            <mat-error>{{ err('email') }}</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Phone</mat-label>
            <input matInput formControlName="phone" placeholder="+91 98765 43210" />
            <mat-error>{{ err('phone') }}</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Hire date</mat-label>
            <input matInput type="date" formControlName="hireDate" [max]="today" />
            <mat-error>{{ err('hireDate') }}</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Department</mat-label>
            <mat-select formControlName="department">
              @for (d of departments; track d) { <mat-option [value]="d">{{ d }}</mat-option> }
            </mat-select>
            <mat-error>{{ err('department') }}</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Job title</mat-label>
            <input matInput formControlName="jobTitle" />
            <mat-error>{{ err('jobTitle') }}</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Status</mat-label>
            <mat-select formControlName="status">
              @for (s of statuses; track s.value) { <mat-option [value]="s.value">{{ s.label }}</mat-option> }
            </mat-select>
          </mat-form-field>
        </div>
        @if (serverError()) { <p class="server-error" role="alert">{{ serverError() }}</p> }
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Cancel</button>
        <button mat-flat-button type="submit" [disabled]="form.invalid || saving()">
          {{ data ? 'Save changes' : 'Add employee' }}
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: `
    .grid { display: grid; grid-template-columns: 1fr 1fr; column-gap: 16px; padding-top: 8px; }
    .wide { grid-column: 1 / -1; }
    .server-error { color: var(--danger); margin: 0; font-size: 0.9rem; }
    @media (max-width: 560px) { .grid { grid-template-columns: 1fr; } }
  `,
})
export class EmployeeDialogComponent {
  protected data = inject<Employee | null>(MAT_DIALOG_DATA);
  private ref = inject<MatDialogRef<EmployeeDialogComponent, boolean>>(MatDialogRef);
  private svc = inject(EmployeeService);
  private fb = inject(NonNullableFormBuilder);

  readonly today = todayIso();
  readonly departments = DEPARTMENTS;
  readonly statuses = (Object.keys(STATUS_LABEL) as (keyof typeof STATUS_LABEL)[]).map((value) => ({ value, label: STATUS_LABEL[value] }));
  saving = signal(false);
  serverError = signal('');

  form = this.fb.group({
    firstName: [this.data?.firstName ?? '', [Validators.required, Validators.maxLength(80)]],
    lastName: [this.data?.lastName ?? '', [Validators.required, Validators.maxLength(80)]],
    email: [this.data?.email ?? '', [Validators.required, Validators.email, Validators.maxLength(150)]],
    phone: [this.data?.phone ?? '', [Validators.required, Validators.pattern(/^[+0-9()\-\s]{7,20}$/)]],
    department: [this.data?.department ?? '', Validators.required],
    jobTitle: [this.data?.jobTitle ?? '', [Validators.required, Validators.maxLength(100)]],
    hireDate: [this.data?.hireDate ?? '', [Validators.required, notFuture]],
    status: [this.data?.status ?? ('ACTIVE' as Employee['status']), Validators.required],
  });

  err(name: string): string {
    const e = this.form.get(name)?.errors;
    if (!e) return '';
    if (e['server']) return e['server'];
    if (e['required']) return 'This field is required';
    if (e['email']) return 'Enter a valid email address';
    if (e['pattern']) return 'Enter a valid phone number';
    if (e['future']) return 'Hire date cannot be in the future';
    if (e['maxlength']) return 'This value is too long';
    return 'This value is not valid';
  }

  save(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    this.serverError.set('');
    const body: EmployeeInput = this.form.getRawValue();
    const request = this.data ? this.svc.update(this.data.id, body) : this.svc.create(body);
    request.subscribe({
      next: () => this.ref.close(true),
      error: (e: HttpErrorResponse) => {
        this.saving.set(false);
        const fields = e.error?.errors as Record<string, string> | undefined;
        if ((e.status === 400 || e.status === 409) && fields) {
          for (const [field, message] of Object.entries(fields)) {
            const control = this.form.get(field);
            if (control) { control.setErrors({ server: message }); control.markAsTouched(); }
          }
        } else {
          this.serverError.set(e.error?.message ?? 'Could not save the employee. Try again.');
        }
      },
    });
  }
}
