import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { API } from './api';
import { EmployeePage, ReportFilter, ReportSummary, SortDir } from './models';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private http = inject(HttpClient);
  private url = `${API}/reports/employees`;

  private params(f: ReportFilter): HttpParams {
    let p = new HttpParams().set('from', f.from).set('to', f.to);
    if (f.status) p = p.set('status', f.status);
    return p;
  }

  summary(f: ReportFilter) {
    return this.http.get<ReportSummary>(`${this.url}/summary`, { params: this.params(f) });
  }

  list(f: ReportFilter, page: number, size: number, sort: string, dir: SortDir) {
    const params = this.params(f).set('page', page).set('size', size).set('sort', sort).set('dir', dir);
    return this.http.get<EmployeePage>(this.url, { params });
  }

  /** Fetched as a blob through HttpClient so the JWT interceptor attaches the token. */
  exportFile(kind: 'xlsx' | 'pdf', f: ReportFilter) {
    return this.http.get(`${this.url}/export.${kind}`, { params: this.params(f), responseType: 'blob' });
  }
}
