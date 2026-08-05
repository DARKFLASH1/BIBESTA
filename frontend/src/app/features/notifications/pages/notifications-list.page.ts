import { Component } from '@angular/core';

@Component({
  selector: 'app-notifications-list',
  standalone: true,
  template: `
    <div style="padding:2rem; color:var(--text-main);">
      <h1>🔔 Notifications</h1>
      <p style="color:var(--text-muted); margin-top:0.5rem;">Cette page est en cours de développement.</p>
    </div>
  `
})
export class NotificationsListPage {}
