import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models/auth.model';
import { LucideLibrary, LucideEye, LucideEyeOff } from '@lucide/angular';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, LucideLibrary, LucideEye, LucideEyeOff],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  credentials: LoginRequest = {
    identifiant: '',
    motDePasse: ''
  };

  erreur      = signal('');
  chargement  = signal(false);
  motDePasseVisible = false; // toggle afficher/masquer mot de passe

  onLogin(): void {
    if (!this.credentials.identifiant || !this.credentials.motDePasse) {
      this.erreur.set('Veuillez remplir tous les champs.');
      return;
    }

    this.erreur.set('');
    this.chargement.set(true);

    this.authService.login(this.credentials).subscribe({
      next: () => {
        this.router.navigate(['/books']);
      },
      error: (err) => {
        this.chargement.set(false);
        this.erreur.set(err.error?.message || 'Identifiant ou mot de passe incorrect.');
      }
    });
  }
}