import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { vi, describe, it, expect } from 'vitest';
import { LoginComponent } from './login';
import { AuthService } from '../../../core/services/auth.service';

describe('LoginComponent (P3.7 — spec minimal vitest, ne teste que onLogin sans réseau)', () => {
  it('ne soumet pas quand un champ est vide', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const c = fixture.componentInstance as any;
    c.credentials = { identifiant: '', motDePasse: 'x' };
    c.onLogin();
    expect((c as any).erreur()).toContain('remplir');
  });
});
