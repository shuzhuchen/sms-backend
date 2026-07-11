import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <section class="login-panel">
      <h1>Sign in</h1>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <label>
          Username
          <input type="text" formControlName="username" autocomplete="username">
        </label>

        <label>
          Password
          <input type="password" formControlName="password" autocomplete="current-password">
        </label>

        @if (errorMessage) {
          <div class="alert">{{ errorMessage }}</div>
        }

        <button class="primary" type="submit" [disabled]="form.invalid || loading">
          {{ loading ? 'Signing in...' : 'Login' }}
        </button>
      </form>
    </section>
  `,
  styles: [`
    .login-panel {
      background: #ffffff;
      border: 1px solid #dbe3ef;
      border-radius: 8px;
      margin: 48px auto;
      max-width: 420px;
      padding: 28px;
    }

    h1 {
      font-size: 28px;
      margin: 0 0 22px;
    }

    form,
    label {
      display: grid;
      gap: 10px;
    }

    form {
      gap: 18px;
    }

    label {
      color: #374151;
      font-weight: 700;
    }
  `]
})
export class LoginComponent {
  loading = false;
  errorMessage = '';

  readonly form = this.formBuilder.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  submit(): void {
    if (this.form.invalid) {
      this.errorMessage = 'Username and password are required.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/students']),
      error: () => {
        this.errorMessage = 'Login failed. Check your username and password.';
        this.loading = false;
      }
    });
  }
}
