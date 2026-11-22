import { Component, HostListener, OnDestroy, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/auth.service';

const IDLE_MS = 30 * 60 * 1000;
const WARN_MS = 28 * 60 * 1000;

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, MatButtonModule, MatIconModule],
  template: `
    <header class="bar">
      <span class="logo">Roster</span>
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
    .spacer { flex: 1; }
    .who { display: flex; align-items: center; gap: 10px; }
    .role { background: var(--teal-soft); color: var(--teal); padding: 2px 10px; border-radius: 999px; font-size: 0.8rem; font-weight: 600; }
    main { max-width: 1180px; margin: 0 auto; padding: 32px; }
    @media (max-width: 640px) { .bar { padding: 10px 16px; } .who strong { display: none; } main { padding: 16px; } }
  `,
})
export class ShellComponent implements OnInit, OnDestroy {
  protected auth = inject(AuthService);
  private snack = inject(MatSnackBar);
  private warnTimer?: ReturnType<typeof setTimeout>;
  private idleTimer?: ReturnType<typeof setTimeout>;
  private lastReset = 0;

  ngOnInit(): void { this.resetTimers(); }
  ngOnDestroy(): void { this.clearTimers(); }

  @HostListener('document:click')
  @HostListener('document:keydown')
  @HostListener('document:mousemove')
  onActivity(): void {
    if (Date.now() - this.lastReset > 1000) this.resetTimers();
  }

  private resetTimers(): void {
    this.clearTimers();
    this.lastReset = Date.now();
    this.warnTimer = setTimeout(
      () => this.snack.open('You will be signed out in 2 minutes due to inactivity.', 'Stay signed in', { duration: 120000 }),
      WARN_MS,
    );
    this.idleTimer = setTimeout(() => {
      this.snack.open('You were signed out after 30 minutes of inactivity.', 'Dismiss', { duration: 8000 });
      this.auth.logout();
    }, IDLE_MS);
  }

  private clearTimers(): void {
    clearTimeout(this.warnTimer);
    clearTimeout(this.idleTimer);
  }
}
