import { Component, input, output } from '@angular/core';
import {
  LucideCalendar,
  LucideGlobe,
  LucideBookOpen,
  LucidePencil,
  LucideX
} from '@lucide/angular';
import { Livre } from '../../../core/models/entities.model';

@Component({
  selector: 'app-livre-card',
  standalone: true,
  imports: [LucideCalendar, LucideGlobe, LucideBookOpen, LucidePencil, LucideX],
  templateUrl: './livre-card.html',
  styleUrl: './livre-card.scss'
})
export class LivreCardComponent {

  // ── Entrées (ce que le parent doit fournir) ──────────
  livre = input.required<Livre>();
  estBibliothecaire = input(false);   // valeur par défaut : false si le parent ne précise rien

  // ── Sorties (ce que la carte "annonce" au parent) ─────
  // output() remplace @Output() + EventEmitter en Angular 17+.
  // La carte ne modifie JAMAIS les données elle-même :
  // elle se contente de dire "on a cliqué sur modifier", et laisse
  // le parent (books-list.ts) décider quoi faire — c'est lui qui a le service HTTP.
  modifier = output<Livre>();
  supprimer = output<Livre>();

  onModifier(): void {
    this.modifier.emit(this.livre());
  }

  onSupprimer(): void {
    this.supprimer.emit(this.livre());
  }
}