import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html'
})
export class login {

  credentials: LoginRequest = {
    identifiant: '',
    motDePasse: ''
  };

  erreur: string = '';
  chargement: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onLogin(): void {
    this.erreur = '';
    this.chargement = true;

    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        this.chargement = false;
        console.log('Connexion réussie :', response);  // debug
        if (response.role === 'BIBLIOTHECAIRE') {
          this.router.navigate(['/bibliothecaire/dashboard']);
        } else {
          this.router.navigate(['/lecteur/dashboard']);
        }
      },
      error: (err) => {
        this.chargement = false;
        console.log('Erreur :', err);  // debug
        this.erreur = 'Identifiant ou mot de passe incorrect';
      }
    });
  }
}