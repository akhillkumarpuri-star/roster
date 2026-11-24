import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from './auth.service';

/** UC-LOGIN-04: warn after 28 minutes without activity, sign out after 30. */
export const IDLE_MS = 30 * 60 * 1000;
export const WARN_MS = 28 * 60 * 1000;

const ACTIVITY_EVENTS = ['click', 'keydown', 'mousemove'] as const;

@Injectable({ providedIn: 'root' })
export class IdleService {
  private auth = inject(AuthService);
  private snack = inject(MatSnackBar);

  private warnTimer?: ReturnType<typeof setTimeout>;
  private idleTimer?: ReturnType<typeof setTimeout>;
  private lastReset = 0;

  // Throttled to once a second: mousemove fires constantly and does not need to re-arm two timers each time.
  private readonly onActivity = (): void => {
    if (Date.now() - this.lastReset > 1000) this.reset();
  };

  /** Start watching. Safe to call again; it restarts cleanly. */
  start(): void {
    this.stop();
    ACTIVITY_EVENTS.forEach((e) => document.addEventListener(e, this.onActivity, { passive: true }));
    this.reset();
  }

  stop(): void {
    ACTIVITY_EVENTS.forEach((e) => document.removeEventListener(e, this.onActivity));
    clearTimeout(this.warnTimer);
    clearTimeout(this.idleTimer);
  }

  private reset(): void {
    clearTimeout(this.warnTimer);
    clearTimeout(this.idleTimer);
    this.lastReset = Date.now();
    this.auth.touch();
    this.warnTimer = setTimeout(
      () => this.snack.open('You will be signed out in 2 minutes due to inactivity.', 'Stay signed in', { duration: 120000 }),
      WARN_MS,
    );
    this.idleTimer = setTimeout(() => {
      this.snack.open('You were signed out after 30 minutes of inactivity.', 'Dismiss', { duration: 8000 });
      this.auth.logout();
    }, IDLE_MS);
  }
}
