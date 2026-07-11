import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './auth/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <header class="topbar">
      <a routerLink="/students" class="brand">Student Management</a>
      @if (authService.isAuthenticated()) {
        <button class="secondary" type="button" (click)="logout()">Logout</button>
      }
    </header>
    <main>
      <router-outlet />
    </main>
  `,
  styles: [`
    .topbar {
      align-items: center;
      background: #ffffff;
      border-bottom: 1px solid #dbe3ef;
      display: flex;
      justify-content: space-between;
      min-height: 64px;
      padding: 0 24px;
    }

    .brand {
      color: #111827;
      font-size: 18px;
      font-weight: 800;
      text-decoration: none;
    }

    main {
      margin: 0 auto;
      max-width: 1120px;
      padding: 28px 20px;
    }
  `]
})
export class AppComponent {
  constructor(public readonly authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}
