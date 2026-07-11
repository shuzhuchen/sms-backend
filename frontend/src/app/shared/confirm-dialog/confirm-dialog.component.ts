import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  template: `
    <div class="backdrop" role="presentation">
      <section class="dialog" role="dialog" aria-modal="true" aria-labelledby="confirm-title">
        <h2 id="confirm-title">{{ title }}</h2>
        <p>{{ message }}</p>
        <div class="actions">
          <button class="secondary" type="button" (click)="cancel.emit()">Cancel</button>
          <button class="danger" type="button" (click)="confirm.emit()">Delete</button>
        </div>
      </section>
    </div>
  `,
  styles: [`
    .backdrop {
      align-items: center;
      background: rgba(17, 24, 39, 0.42);
      display: flex;
      inset: 0;
      justify-content: center;
      padding: 20px;
      position: fixed;
      z-index: 20;
    }

    .dialog {
      background: #ffffff;
      border-radius: 8px;
      box-shadow: 0 20px 50px rgba(15, 23, 42, 0.24);
      max-width: 420px;
      padding: 24px;
      width: 100%;
    }

    h2 {
      font-size: 22px;
      margin: 0 0 10px;
    }

    p {
      color: #4b5563;
      margin: 0 0 22px;
    }

    .actions {
      display: flex;
      gap: 10px;
      justify-content: flex-end;
    }
  `]
})
export class ConfirmDialogComponent {
  @Input() title = 'Confirm delete';
  @Input() message = 'Are you sure?';
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
}
