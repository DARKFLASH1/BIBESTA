import { Component, Input } from '@angular/core';
import { Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-confirmation-dialog',
  template: `
    <div class="confirmation-backdrop" (click)="onCancel()"></div>
    <div class="confirmation-modal">
      <h2>Confirmation</h2>
      <p>{{ message }}</p>
      <button class="btn" (click)="confirm()">Confirm</button>
      <button class="btn btn-secondary" (click)="onCancel()">Cancel</button>
    </div>
  `,
  styleUrls: ['./confirmation-dialog.component.scss']
})
export class ConfirmationDialogComponent {
  @Input() message = '';
  @Output() confirmed = new EventEmitter<boolean>();

  confirm(): void {
    this.confirmed.emit(true);
  }

  onCancel(): void {
    this.confirmed.emit(false);
  }
}