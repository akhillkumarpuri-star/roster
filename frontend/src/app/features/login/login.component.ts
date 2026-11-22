import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatCheckboxModule],
  template: `
    <div class="layout">
      <aside class="brand">
        <span class="logo">Roster</span>
        <div>
          <h1>Everyone on the team, in one list.</h1>
          <p>Add people, keep their details current, and see who is active, on leave or has moved on.</p>
        </div>
        <small>Demo accounts: admin / Admin&#64;123 &nbsp;&middot;&nbsp; viewer / Viewer&#64;123</small>
      </aside>

      <main class="panel">
        <form class="card" [formGroup]="form" (ngSubmit)="submit()" novalidate>
          <h2>Sign in</h2>
          <p class="hint">Use your Roster account to continue.</p>

          @if (error()) { <div class="alert" role="alert">{{ error() }}</div> }

          <mat-form-field appearance="outline">
            <mat-label>Username</mat-label>
            <input matInput formControlName="username" autocomplete="username" />
            @if (form.controls.username.hasError('required')) { <mat-error>Enter your username</mat-error> }
            @else if (form.controls.username.hasError('minlength')) { <mat-error>Username needs at least 3 characters</mat-error> }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Password</mat-label>
            <input matInput formControlName="password" [type]="hide() ? 'password' : 'text'" autocomplete="current-password" />
            <button mat-icon-button matSuffix type="button" (click)="hide.set(!hide())"
                    [attr.aria-label]="hide() ? 'Show password' : 'Hide password'">
              <mat-icon>{{ hide() ? 'visibility' : 'visibility_off' }}</mat-icon>
            </button>
            @if (form.controls.password.hasError('required')) { <mat-error>Enter your password</mat-error> }
            @else if (form.controls.password.hasError('minlength')) { <mat-error>Password needs at least 6 characters</mat-error> }
          </mat-form-field>

          <mat-checkbox formControlName="remember">Remember my username</mat-checkbox>

          <button mat-flat-button type="submit" [disabled]="form.invalid || loading()">
            {{ loading() ? 'Signing in…' : 'Sign in' }}
          </button>
        </form>
      </main>
    </div>
  `,
  styles: `
    .layout { display: grid; grid-template-columns: minmax(320px, 5fr) 6fr; min-height: 100vh; }
    .brand { background: var(--ink); color: #fff; padding: 48px; display: flex; flex-direction: column; justify-content: space-between; gap: 32px; }
    .logo { font-weight: 700; font-size: 1.5rem; letter-spacing: -0.02em; }
    h1 { font-size: clamp(2rem, 3.6vw, 3.2rem); line-height: 1.08; letter-spacing: -0.03em; margin: 0 0 16px; max-width: 14ch; font-weight: 600; }
    .brand p { margin: 0; max-width: 38ch; line-height: 1.55; color: #b8c4d3; }
    .brand small { color: #8fa1b6; }
    .panel { display: grid; place-items: center; padding: 32px; }
    .card { width: min(420px, 100%); display: flex; flex-direction: column; gap: 6px; }
    h2 { margin: 0; font-size: 1.75rem; letter-spacing: -0.02em; }
    .hint { margin: 0 0 20px; color: var(--ink-2); }
    mat-form-field { width: 100%; }
    button[type='submit'] { height: 48px; margin-top: 16px; }
    .alert { background: #fdecea; color: var(--danger); border-left: 3px solid var(--danger); padding: 10px 12px; border-radius: 4px; margin-bottom: 12px; font-size: 0.9rem; }
    @media (max-width: 800px) {
      .layout { grid-template-columns: 1fr; }
      .brand { padding: 24px; gap: 16px; }
      h1 { font-size: 1.8rem; }
      .brand small { display: none; }
    }
  `,
})
export class LoginComponent {
  private fb = inject(NonNullableFormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  hide = signal(true);
  loading = signal(false);
  error = signal('');

  form = this.fb.group({
    username: [this.auth.rememberedUsername(), [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    remember: [!!this.auth.rememberedUsername()],
  });

  submit(): void {
    if (this.form.invalid || this.loading()) return;
    const { username, password, remember } = this.form.getRawValue();
    this.loading.set(true);
    this.error.set('');
    this.auth.login(username, password, remember).subscribe({
      next: () => this.router.navigateByUrl('/employees'),
      error: (e) => {
        this.loading.set(false);
        this.error.set(e.status === 401
          ? 'Invalid username or password.'
          : 'Cannot reach the server. Check that the API is running on port 8080.');
      },
    });
  }
}
