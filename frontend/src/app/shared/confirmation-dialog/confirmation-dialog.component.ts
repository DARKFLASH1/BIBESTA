import {
  AfterViewInit,
  Component,
  ElementRef,
  HostListener,
  input,
  output,
  viewChild
} from '@angular/core';
import {
  LucideAlertTriangle,
  LucideCheck,
  LucideHelpCircle,
  LucideTrash2,
  LucideX
} from '@lucide/angular';

// Deux styles visuels possibles pour la modale :
// - 'default' : action neutre (ex: "Confirmer le retour")
// - 'danger'  : action destructrice (ex: "Supprimer un livre", "Annuler une réservation")
// On utilise ce "variant" pour choisir automatiquement la bonne icône
// et la bonne couleur de bouton (--accent vs --danger), sans que chaque
// page ait à redéfinir ses propres styles.
export type ConfirmationVariant = 'default' | 'danger';

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [LucideAlertTriangle, LucideCheck, LucideHelpCircle, LucideTrash2, LucideX],
  templateUrl: './confirmation-dialog.component.html',
  styleUrl: './confirmation-dialog.component.scss'
})
export class ConfirmationDialogComponent implements AfterViewInit {

  // ── Entrées (ce que le parent doit fournir) ──────────────────────────
  // Angular 19 : input() remplace @Input(). On lit la valeur en appelant
  // le signal comme une fonction, ex: this.title() et non this.title.

  title = input('Confirmation');
  // Titre affiché en haut de la modale. Valeur par défaut si le parent
  // ne précise rien.

  message = input.required<string>();
  // input.required : le parent DOIT fournir un message, sinon erreur de
  // compilation. Logique : une confirmation sans texte n'a pas de sens.

  confirmLabel = input('Confirmer');
  cancelLabel = input('Annuler');
  // Les libellés des boutons sont personnalisables : "Supprimer" au lieu
  // de "Confirmer" est souvent plus clair pour l'utilisateur.

  variant = input<ConfirmationVariant>('default');

  // ── Sortie (ce que la modale annonce au parent) ──────────────────────
  // Un seul événement : true = l'utilisateur a confirmé, false = il a annulé.
  // C'est TOUJOURS le parent qui décide quoi faire ensuite (appeler le
  // service HTTP, fermer la modale, etc.) — la modale ne fait qu'informer.
  confirmed = output<boolean>();

  // ── Références vers les boutons (pour le piège de focus clavier) ────
  // viewChild() est l'équivalent signal de @ViewChild(). On récupère les
  // éléments DOM des deux boutons pour pouvoir les focus() manuellement.
  private cancelButton = viewChild.required<ElementRef<HTMLButtonElement>>('cancelBtn');
  private confirmButton = viewChild.required<ElementRef<HTMLButtonElement>>('confirmBtn');

  ngAfterViewInit(): void {
    // Dès que la modale apparaît à l'écran, on place le focus clavier sur
    // le bouton "Annuler" (l'action la plus sûre par défaut). C'est le
    // début du "focus trap" : l'utilisateur au clavier atterrit directement
    // dans la modale, pas sur un élément caché derrière.
    this.cancelButton().nativeElement.focus();
  }

  onConfirm(): void {
    this.confirmed.emit(true);
  }

  onCancel(): void {
    this.confirmed.emit(false);
  }

  // ── Accessibilité clavier : Échap ferme toujours la modale ───────────
  // @HostListener écoute un événement du DOCUMENT entier (pas seulement
  // à l'intérieur du composant) : même si le focus est ailleurs par
  // accident, Échap fonctionne toujours. C'est une règle non négociable
  // du design system (§8.1).
  @HostListener('document:keydown.escape')
  onEscapePressed(): void {
    this.onCancel();
  }

  // ── Piège de focus (Tab reste à l'intérieur de la modale) ───────────
  // Il n'y a que 2 éléments focusables dans cette modale : les 2 boutons.
  // On "boucle" donc manuellement entre eux plutôt que de laisser Tab
  // sortir vers le reste de la page (ce qui serait piégeant pour un
  // utilisateur au clavier ou un lecteur d'écran).

  onTabFromCancel(event: Event): void {
    // Si l'utilisateur fait Shift+Tab depuis "Annuler" (le 1er bouton),
    // on le renvoie sur "Confirmer" (le dernier) au lieu de sortir de la modale.
    // On caste en KeyboardEvent car (keydown.tab) est toujours un KeyboardEvent
    // mais Angular strict templates le type comme Event générique.
    const kbEvent = event as KeyboardEvent;
    if (kbEvent.shiftKey) {
      kbEvent.preventDefault();
      this.confirmButton().nativeElement.focus();
    }
  }

  onTabFromConfirm(event: Event): void {
    // Si l'utilisateur fait Tab (sans Shift) depuis "Confirmer" (le dernier
    // bouton), on le renvoie sur "Annuler" (le premier) plutôt que de sortir.
    const kbEvent = event as KeyboardEvent;
    if (!kbEvent.shiftKey) {
      kbEvent.preventDefault();
      this.cancelButton().nativeElement.focus();
    }
  }
}