import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading-skeleton',
  standalone: true,
  template: `
    <div class="skeleton-list" aria-label="Loading">
      @for (row of rowsArray; track row) {
        <div class="skeleton-row"></div>
      }
    </div>
  `,
  styles: [`
    .skeleton-list {
      display: grid;
      gap: 10px;
    }

    .skeleton-row {
      animation: pulse 1.2s ease-in-out infinite;
      background: linear-gradient(90deg, #eef2f7 0%, #f8fafc 45%, #eef2f7 100%);
      border-radius: 6px;
      height: 44px;
    }

    @keyframes pulse {
      0% {
        opacity: 0.62;
      }

      50% {
        opacity: 1;
      }

      100% {
        opacity: 0.62;
      }
    }
  `]
})
export class LoadingSkeletonComponent {
  @Input() rows = 3;

  get rowsArray(): number[] {
    return Array.from({ length: this.rows }, (_, index) => index);
  }
}
