import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { API } from './api';
import { Employee, EmployeeInput, EmployeePage } from './models';

export interface ListQuery { page: number; size: number; sort: string; dir: 'asc' | 'desc'; q: string; }

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private http = inject(HttpClient);
  private url = `${API}/employees`;

  list(p: ListQuery) {
    const params = new HttpParams().set('page', p.page).set('size', p.size).set('sort', p.sort).set('dir', p.dir).set('q', p.q);
    return this.http.get<EmployeePage>(this.url, { params });
  }
  create(body: EmployeeInput) { return this.http.post<Employee>(this.url, body); }
  update(id: number, body: EmployeeInput) { return this.http.put<Employee>(`${this.url}/${id}`, body); }
  remove(id: number) { return this.http.delete<void>(`${this.url}/${id}`); }
}
