import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class DashboardComponent {

  nomComplet = '';
  role = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.nomComplet = this.authService.getNomComplet();
    this.role = this.authService.getRole() || '';
  }

  logout(): void {
    this.authService.logout();
  }
}