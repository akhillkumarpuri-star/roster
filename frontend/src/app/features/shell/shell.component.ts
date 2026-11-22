import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/auth.service';
import { IdleService } from '../../core/idle.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatButtonModule, MatIconModule],
  template: `
    <header class="bar">
      <span class="logo">Roster</span>
      <nav class="nav" aria-label="Main">
        <a routerLink="/employees" routerLinkActive="active">Employees</a>
        <a routerLink="/report" routerLinkActive="active">Report</a>
      </nav>
      <span class="spacer"></span>
      <div class="who">
        <strong>{{ auth.session()?.fullName }}</strong>
        <span class="role">{{ auth.isAdmin() ? 'Admin' : 'Viewer' }}</span>
      </div>
      <button mat-stroked-button (click)="auth.logout()"><mat-icon>logout</mat-icon>Sign out</button>
    </header>
    <main><router-outlet /></main>
  `,
  styles: `
    .bar { display: flex; align-items: center; gap: 16px; padding: 12px 32px; background: var(--surface); border-bottom: 1px solid var(--line); position: sticky; top: 0; z-index: 5; }
    .logo { font-weight: 700; font-size: 1.25rem; letter-spacing: -0.02em; }
    .nav { display: flex; gap: 4px; margin-left: 16px; }
    .nav a { padding: 6px 14px; border-radius: 999px; color: var(--ink-2); text-decoration: none; font-weight: 600; }
    .nav a:hover { background: var(--gray-soft); }
    .nav a.active { background: var(--teal-soft); color: var(--teal); }
    .spacer { flex: 1; }
    .who { display: flex; align-items: center; gap: 10px; }
    .role { background: var(--teal-soft); color: var(--teal); padding: 2px 10px; border-radius: 999px; font-size: 0.8rem; font-weight: 600; }
    main { max-width: 1180px; margin: 0 auto; padding: 32px; }
    @media (max-width: 640px) { .bar { padding: 10px 16px; } .who strong { display: none; } main { padding: 16px; } }
  `,
})
export class ShellComponent implements OnInit, OnDestroy {
  protected auth = inject(AuthService);
  private idle = inject(IdleService);

  ngOnInit(): void { this.idle.start(); }
  ngOnDestroy(): void { this.idle.stop(); }
}
