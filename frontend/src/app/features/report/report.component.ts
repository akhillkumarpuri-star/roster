import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { saveBlob } from '../../core/download';
import { ReportService } from '../../core/report.service';
import { Employee, EmployeeStatus, ReportFilter, ReportSummary, SortDir, STATUS_LABEL } from '../../core/models';

/** Local yyyy-MM-dd. Do NOT use toISOString(): it converts to UTC and can send the previous day in IST. */
function toIso(d: Date): string {
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mm}-${dd}`;
}

@Component({
  selector: 'app-report',
  imports: [
    DatePipe, ReactiveFormsModule, MatDatepickerModule, MatFormFieldModule, MatSelectModule, MatButtonModule,
    MatProgressBarModule, MatIconModule, MatTableModule, MatSortModule, MatPaginatorModule,
  ],
  template: `
    <section>
      <header class="head">
        <h1>Employee report</h1>
        <p>Hires between the chosen dates, filtered by status.</p>
      </header>

      <form class="filters" [formGroup]="form">
        <mat-form-field appearance="outline" subscriptSizing="dynamic">
          <mat-label>Hire date range</mat-label>
          <mat-date-range-input [rangePicker]="picker">
            <input matStartDate formControlName="from" placeholder="From" aria-label="From date" />
            <input matEndDate formControlName="to" placeholder="To" aria-label="To date" />
          </mat-date-range-input>
          <mat-datepicker-toggle matIconSuffix [for]="picker" />
          <mat-date-range-picker #picker />
        </mat-form-field>

        <mat-form-field appearance="outline" subscriptSizing="dynamic">
          <mat-label>Status</mat-label>
          <mat-select formControlName="status">
            <mat-option value="">All statuses</mat-option>
            @for (s of statuses; track s) { <mat-option [value]="s">{{ label[s] }}</mat-option> }
          </mat-select>
        </mat-form-field>

        <div class="presets" role="group" aria-label="Date presets">
          <button mat-stroked-button type="button" (click)="preset('month')">This month</button>
          <button mat-stroked-button type="button" (click)="preset('year')">This year</button>
          <button mat-stroked-button type="button" (click)="preset('all')">All time</button>
        </div>

        <div class="exports" role="group" aria-label="Export">
          <button mat-flat-button type="button" (click)="exportFile('xlsx')" [disabled]="exporting() || total() === 0">
            <mat-icon>table_view</mat-icon>Export Excel
          </button>
          <button mat-flat-button type="button" (click)="exportFile('pdf')" [disabled]="exporting() || total() === 0">
            <mat-icon>picture_as_pdf</mat-icon>Export PDF
          </button>
        </div>
      </form>

      @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
      @if (loading()) { <mat-progress-bar mode="indeterminate" /> }

      @if (summary(); as s) {
        <div class="cards">
          <div class="card main"><span>Total records</span><strong>{{ s.total }}</strong></div>
          @for (st of statuses; track st) {
            <div class="card"><span>{{ label[st] }}</span><strong>{{ s.byStatus[st] }}</strong></div>
          }
        </div>

        <div class="dept">
          <h2>By department</h2>
          @if (s.byDepartment.length === 0) {
            <p class="muted">No employees were hired in this range.</p>
          } @else {
            <ul>
              @for (d of s.byDepartment; track d.department) {
                <li>
                  <span class="name">{{ d.department }}</span>
                  <span class="bar"><span class="fill" [style.width.%]="(d.count / s.total) * 100"></span></span>
                  <span class="num">{{ d.count }}</span>
                </li>
              }
            </ul>
          }
        </div>
      }

      <div class="table-wrap">
        @if (tableLoading()) { <mat-progress-bar mode="indeterminate" /> }
        <div class="scroll">
          <table mat-table [dataSource]="rows()" matSort matSortActive="hireDate" matSortDirection="desc"
                 matSortDisableClear (matSortChange)="onSort($event)">
            <ng-container matColumnDef="name">
              <th mat-header-cell *matHeaderCellDef mat-sort-header="lastName">Name</th>
              <td mat-cell *matCellDef="let e"><strong>{{ e.firstName }} {{ e.lastName }}</strong></td>
            </ng-container>
            <ng-container matColumnDef="email">
              <th mat-header-cell *matHeaderCellDef mat-sort-header="email">Email</th>
              <td mat-cell *matCellDef="let e">{{ e.email }}</td>
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
              <td mat-cell *matCellDef="let e"><span [class]="'chip ' + e.status">{{ statusLabel(e.status) }}</span></td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns"></tr>
          </table>
        </div>
        @if (!tableLoading() && rows().length === 0) {
          <div class="empty">No employees match these filters.</div>
        }
        <mat-paginator [length]="total()" [pageSize]="pageSize" [pageIndex]="pageIndex()" hidePageSize
                       showFirstLastButtons (page)="onPage($event)" />
      </div>
    </section>
  `,
  styles: `
    h1 { margin: 0; font-size: 1.75rem; letter-spacing: -0.02em; }
    .head p { margin: 4px 0 20px; color: var(--ink-2); }
    .filters { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; margin-bottom: 20px; }
    .presets, .exports { display: flex; gap: 8px; flex-wrap: wrap; }
    .exports { margin-left: auto; }
    .cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 16px; margin: 20px 0; }
    .card { background: var(--surface); border: 1px solid var(--line); border-radius: 12px; padding: 16px 20px; display: flex; flex-direction: column; gap: 6px; }
    .card span { color: var(--ink-2); font-size: 0.9rem; }
    .card strong { font-size: 2rem; letter-spacing: -0.02em; }
    .card.main { background: var(--teal-soft); border-color: transparent; }
    .card.main strong { color: var(--teal); }
    .dept { background: var(--surface); border: 1px solid var(--line); border-radius: 12px; padding: 16px 20px; }
    .dept h2 { margin: 0 0 12px; font-size: 1.05rem; }
    ul { list-style: none; margin: 0; padding: 0; display: grid; gap: 10px; }
    li { display: grid; grid-template-columns: 140px 1fr 40px; gap: 12px; align-items: center; }
    .bar { background: var(--gray-soft); border-radius: 999px; height: 10px; overflow: hidden; }
    .fill { display: block; height: 100%; background: var(--teal); border-radius: 999px; }
    .num { text-align: right; font-weight: 600; }
    .muted { color: var(--ink-2); margin: 0; }
    .error { color: var(--danger); }
    .table-wrap { background: var(--surface); border: 1px solid var(--line); border-radius: 8px; overflow: hidden; margin-top: 20px; }
    .scroll { overflow-x: auto; }
    table { width: 100%; background: transparent; }
    .chip { padding: 3px 10px; border-radius: 999px; font-size: 0.8rem; font-weight: 600; white-space: nowrap; }
    .chip.ACTIVE { background: var(--teal-soft); color: var(--teal); }
    .chip.ON_LEAVE { background: var(--amber-soft); color: var(--amber-ink); }
    .chip.INACTIVE { background: var(--gray-soft); color: var(--ink-2); }
    .empty { padding: 40px 16px; text-align: center; color: var(--ink-2); }
  `,
})
export class ReportComponent implements OnInit {
  private api = inject(ReportService);

  protected statuses: EmployeeStatus[] = ['ACTIVE', 'ON_LEAVE', 'INACTIVE'];
  protected label = STATUS_LABEL;
  protected summary = signal<ReportSummary | null>(null);
  protected loading = signal(false);
  protected tableLoading = signal(false);
  protected exporting = signal(false);
  protected rows = signal<Employee[]>([]);
  protected total = signal(0);
  protected pageIndex = signal(0);
  protected readonly pageSize = 10;
  protected readonly columns = ['name', 'email', 'department', 'jobTitle', 'hireDate', 'status'];
  private sortField = 'hireDate';
  private sortDir: SortDir = 'desc';
  protected error = signal('');

  // UC-RPT-05: default range is the current month.
  protected form = new FormGroup({
    from: new FormControl<Date | null>(this.firstOfMonth()),
    to: new FormControl<Date | null>(new Date()),
    status: new FormControl<EmployeeStatus | ''>('', { nonNullable: true }),
  });

  ngOnInit(): void {
    this.form.valueChanges.subscribe(() => { this.pageIndex.set(0); this.load(); });
    this.load();
  }

  protected preset(kind: 'month' | 'year' | 'all'): void {
    const now = new Date();
    const from = kind === 'month' ? this.firstOfMonth() : kind === 'year' ? new Date(now.getFullYear(), 0, 1) : new Date(1900, 0, 1);
    this.form.patchValue({ from, to: now });
  }

  private firstOfMonth(): Date {
    const n = new Date();
    return new Date(n.getFullYear(), n.getMonth(), 1);
  }

  protected exportFile(kind: 'xlsx' | 'pdf'): void {
    const filter = this.filter();
    if (!filter) return;
    this.exporting.set(true);
    this.error.set('');
    this.api.exportFile(kind, filter).subscribe({
      next: (blob) => { saveBlob(blob, `employee-report-${filter.from}_to_${filter.to}.${kind}`); this.exporting.set(false); },
      error: () => {
        this.exporting.set(false);
        this.error.set(`Could not export to ${kind === 'pdf' ? 'PDF' : 'Excel'}. Please try again.`);
      },
    });
  }

  protected statusLabel(s: EmployeeStatus): string { return STATUS_LABEL[s]; }

  protected onSort(s: Sort): void {
    this.sortField = s.active;
    this.sortDir = s.direction || 'desc';
    this.pageIndex.set(0);
    this.loadTable();
  }

  protected onPage(e: PageEvent): void {
    this.pageIndex.set(e.pageIndex);
    this.loadTable();
  }

  /** Current filter, or null while the range picker is only half filled. */
  private filter(): ReportFilter | null {
    const { from, to, status } = this.form.getRawValue();
    return from && to ? { from: toIso(from), to: toIso(to), status } : null;
  }

  private load(): void {
    const filter = this.filter();
    if (!filter) return;
    this.loading.set(true);
    this.error.set('');
    this.api.summary(filter).subscribe({
      next: (s) => { this.summary.set(s); this.loading.set(false); },
      error: (e) => {
        this.loading.set(false);
        this.error.set(e.error?.message ?? 'Could not load the report. Please try again.');
      },
    });
    this.loadTable();
  }

  private loadTable(): void {
    const filter = this.filter();
    if (!filter) return;
    this.tableLoading.set(true);
    this.api.list(filter, this.pageIndex(), this.pageSize, this.sortField, this.sortDir).subscribe({
      next: (p) => { this.rows.set(p.content); this.total.set(p.totalElements); this.tableLoading.set(false); },
      error: (e) => {
        this.tableLoading.set(false);
        this.rows.set([]);
        this.total.set(0);
        this.error.set(e.error?.message ?? 'Could not load the report. Please try again.');
      },
    });
  }
}
