import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { ShellComponent } from './features/shell/shell.component';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent) },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: 'employees', loadComponent: () => import('./features/employees/employees.component').then((m) => m.EmployeesComponent) },
      { path: 'report', loadComponent: () => import('./features/report/report.component').then((m) => m.ReportComponent) },
      { path: '', pathMatch: 'full', redirectTo: 'employees' },
    ],
  },
  { path: '**', redirectTo: '' },
];
