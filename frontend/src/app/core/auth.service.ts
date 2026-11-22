import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API } from './api';
import { LoginResponse, Session } from './models';

const SESSION_KEY = 'roster.session';
const USERNAME_KEY = 'roster.username';

function isExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    return payload.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  readonly session = signal<Session | null>(this.restore());
  readonly isAdmin = computed(() => this.session()?.role === 'ADMIN');

  login(username: string, password: string, remember: boolean): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API}/auth/login`, { username, password }).pipe(
      tap((res) => {
        const session: Session = { token: res.token, username: res.username, fullName: res.fullName, role: res.role };
        localStorage.setItem(SESSION_KEY, JSON.stringify(session));
        this.session.set(session);
        if (remember) localStorage.setItem(USERNAME_KEY, username);
        else localStorage.removeItem(USERNAME_KEY);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(SESSION_KEY);
    this.session.set(null);
    this.router.navigate(['/login']);
  }

  token(): string | null { return this.session()?.token ?? null; }

  isLoggedIn(): boolean {
    const t = this.token();
    return !!t && !isExpired(t);
  }

  rememberedUsername(): string { return localStorage.getItem(USERNAME_KEY) ?? ''; }

  private restore(): Session | null {
    try {
      const raw = localStorage.getItem(SESSION_KEY);
      const s = raw ? (JSON.parse(raw) as Session) : null;
      return s && !isExpired(s.token) ? s : null;
    } catch {
      return null;
    }
  }
}
