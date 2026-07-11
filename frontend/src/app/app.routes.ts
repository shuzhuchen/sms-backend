import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { StudentListComponent } from './students/student-list/student-list.component';
import { StudentDetailComponent } from './students/student-detail/student-detail.component';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'students', component: StudentListComponent, canActivate: [authGuard] },
  {
    path: 'students/new',
    loadComponent: () => import('./students/student-form/student-form.component').then((component) => component.StudentFormComponent),
    canActivate: [authGuard]
  },
  { path: 'students/:id', component: StudentDetailComponent, canActivate: [authGuard] },
  {
    path: 'students/:id/edit',
    loadComponent: () => import('./students/student-form/student-form.component').then((component) => component.StudentFormComponent),
    canActivate: [authGuard]
  },
  { path: '', pathMatch: 'full', redirectTo: 'students' },
  { path: '**', redirectTo: 'students' }
];
