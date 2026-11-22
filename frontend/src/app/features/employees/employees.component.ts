import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../core/auth.service';
import { EmployeeService } from '../../core/employee.service';
import { Employee, STATUS_LABEL } from '../../core/models';
import { ConfirmDialogComponent } from './confirm-dialog.component';
import { EmployeeDialogComponent } from './employee-dialog.component';

@Component({
  selector: 'app-employees',
  imports: [
    DatePipe, ReactiveFormsModule, MatTableModule, MatSortModule, MatPaginatorModule, MatButtonModule,
    MatIconModule, MatFormFieldModule, MatInputModule, MatProgressBarModule, MatTooltipModule,
  ],
  template: `
    <section>
      <header class="head">
        <div>
          <h1>Employees</h1>
          <p>{{ total() }} {{ total() === 1 ? 'person' : 'people' }} on the roster</p>
        </div>
        @if (auth.isAdmin()) {
          <button mat-flat-button (click)="openForm()"><mat-icon>add</mat-icon>Add employee</button>
        }
      </header>

      <mat-form-field appearance="outline" subscriptSizing="dynamic" class="search">
        <mat-icon matPrefix>search</mat-icon>
        <input matInput [formControl]="search" placeholder="Search by name, email, department or title" aria-label="Search employees" />
        @if (search.value) {
          <button mat-icon-button matSuffix (click)="search.setValue('')" aria-label="Clear search"><mat-icon>close</mat-icon></button>
        }
      </mat-form-field>

      <div class="table-wrap">
        @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
        <div class="scroll">
          <table mat-table [dataSource]="rows()" matSort matSortActive="lastName" matSortDirection="asc"
                 matSortDisableClear (matSortChange)="onSort($event)">

            <ng-container matColumnDef="name">
              <th mat-header-cell *matHeaderCellDef mat-sort-header="lastName">Name</th>
              <td mat-cell *matCellDef="let e">
                <div class="who">
                  <span class="avatar" aria-hidden="true">{{ initials(e) }}</span>
                  <div class="names"><strong>{{ e.firstName }} {{ e.lastName }}</strong><small>{{ e.email }}</small></div>
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="department">
              <th mat-header-cell *matHeaderCellDef mat-sort-header="department">Department</th>
              <td mat-cell *matCellDef="let e">{{ e.department }}</td>
            </ng-container>

            <ng-container matColumnDef="jobTitle">
              <th mat-header-cell *matHeaderCellDef mat-sort-header="jobTitle">Title</th>
              <td mat-cell *matCellDef="let e">{{ e.jobTitle }}</td>
            </ng-container>

            <ng-container matColumnDef="hireDate">
              <th mat-header-cell *matHeaderCellDef mat-sort-header="hireDate">Hired</th>
              <td mat-cell *matCellDef="let e">{{ e.hireDate | date: 'mediumDate' }}</td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef mat-sort-header="status">Status</th>
              <td mat-cell *matCellDef="let e"><span [class]="'chip ' + e.status">{{ label(e.status) }}</span></td>
            </ng-container>

            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let e" class="actions">
                <button mat-icon-button matTooltip="Edit" aria-label="Edit employee" (click)="$event.stopPropagation(); openForm(e)"><mat-icon>edit</mat-icon></button>
                <button mat-icon-button matTooltip="Delete" aria-label="Delete employee" (click)="$event.stopPropagation(); confirmDelete(e)"><mat-icon>delete_outline</mat-icon></button>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="columns()"></tr>
            <tr mat-row *matRowDef="let row; columns: columns()" [class.clickable]="auth.isAdmin()" (click)="onRow(row)"></tr>
          </table>
        </div>

        @if (!loading() && rows().length === 0) {
          <div class="empty">
            @if (search.value) {
              <p>No employees match “{{ search.value }}”.</p>
              <button mat-stroked-button (click)="search.setValue('')">Clear search</button>
            } @else {
              <p>No employees yet.</p>
              @if (auth.isAdmin()) { <button mat-flat-button (click)="openForm()">Add the first employee</button> }
            }
          </div>
        }
        <mat-paginator [length]="total()" [pageSize]="10" [pageIndex]="pageIndex()" hidePageSize
                       showFirstLastButtons (page)="onPage($event)" />
      </div>
    </section>
  `,
  styles: `
    .head { display: flex; align-items: flex-end; justify-content: space-between; gap: 16px; margin-bottom: 20px; }
    h1 { margin: 0; font-size: 2rem; letter-spacing: -0.03em; }
    .head p { margin: 4px 0 0; color: var(--ink-2); }
    .search { width: min(460px, 100%); margin-bottom: 16px; background: var(--surface); border-radius: 4px; }
    .table-wrap { background: var(--surface); border: 1px solid var(--line); border-radius: 8px; overflow: hidden; }
    .scroll { overflow-x: auto; }
    table { width: 100%; background: transparent; }
    tr.clickable { cursor: pointer; }
    tr.clickable:hover td { background: #f7f9fb; }
    .who { display: flex; align-items: center; gap: 12px; padding: 6px 0; }
    .avatar { width: 36px; height: 36px; border-radius: 50%; background: var(--ink); color: #fff; display: grid; place-items: center; font-size: 0.8rem; font-weight: 600; flex: none; }
    .names { display: flex; flex-direction: column; }
    .names small { color: var(--ink-2); }
    .chip { padding: 3px 10px; border-radius: 999px; font-size: 0.8rem; font-weight: 600; white-space: nowrap; }
    .chip.ACTIVE { background: var(--teal-soft); color: var(--teal); }
    .chip.ON_LEAVE { background: var(--amber-soft); color: var(--amber-ink); }
    .chip.INACTIVE { background: var(--gray-soft); color: var(--ink-2); }
    .actions { text-align: right; white-space: nowrap; }
    .empty { padding: 48px 16px; text-align: center; color: var(--ink-2); }
    .empty p { margin: 0 0 16px; }
    @media (max-width: 640px) { .head { flex-direction: column; align-items: stretch; } }
  `,
})
export class EmployeesComponent {
  protected auth = inject(AuthService);
  private svc = inject(EmployeeService);
  private dialog = inject(MatDialog);
  private snack = inject(MatSnackBar);

  private readonly base = ['name', 'department', 'jobTitle', 'hireDate', 'status'];
  readonly columns = computed(() => (this.auth.isAdmin() ? [...this.base, 'actions'] : this.base));

  rows = signal<Employee[]>([]);
  total = signal(0);
  loading = signal(true);
  pageIndex = signal(0);
  search = new FormControl('', { nonNullable: true });

  private sortField = 'lastName';
  private sortDir: 'asc' | 'desc' = 'asc';

  constructor() {
    this.search.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe(() => { this.pageIndex.set(0); this.load(); });
    this.load();
  }

  initials(e: Employee): string { return (e.firstName[0] + e.lastName[0]).toUpperCase(); }
  label(status: Employee['status']): string { return STATUS_LABEL[status]; }

  onSort(s: Sort): void {
    this.sortField = s.active;
    this.sortDir = s.direction || 'asc';
    this.pageIndex.set(0);
    this.load();
  }

  onPage(e: PageEvent): void { this.pageIndex.set(e.pageIndex); this.load(); }

  onRow(row: Employee): void { if (this.auth.isAdmin()) this.openForm(row); }

  openForm(row?: Employee): void {
    this.dialog
      .open<EmployeeDialogComponent, Employee | null, boolean>(EmployeeDialogComponent, {
        data: row ?? null, width: '680px', maxWidth: '95vw',
      })
      .afterClosed()
      .subscribe((saved) => {
        if (!saved) return;
        this.snack.open(row ? 'Employee updated' : 'Employee added', 'Dismiss', { duration: 3500 });
        this.load();
      });
  }

  confirmDelete(e: Employee): void {
    this.dialog
      .open(ConfirmDialogComponent, {
        data: { title: 'Delete employee', message: `Delete ${e.firstName} ${e.lastName}? This cannot be undone.`, confirmLabel: 'Delete' },
      })
      .afterClosed()
      .subscribe((yes) => {
        if (!yes) return;
        this.svc.remove(e.id).subscribe({
          next: () => {
            if (this.rows().length === 1 && this.pageIndex() > 0) this.pageIndex.update((p) => p - 1);
            this.snack.open('Employee deleted', 'Dismiss', { duration: 3500 });
            this.load();
          },
          error: () => this.snack.open('Could not delete the employee. Try again.', 'Dismiss', { duration: 5000 }),
        });
      });
  }

  private load(): void {
    this.loading.set(true);
    this.svc
      .list({ page: this.pageIndex(), size: 10, sort: this.sortField, dir: this.sortDir, q: this.search.value })
      .subscribe({
        next: (p) => { this.rows.set(p.content); this.total.set(p.totalElements); this.loading.set(false); },
        error: () => {
          this.loading.set(false);
          this.snack.open('Could not load employees. Check that the API is running.', 'Dismiss', { duration: 5000 });
        },
      });
  }
}
