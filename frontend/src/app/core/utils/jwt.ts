/**
 * P3.2 / P3.7 — Décodage JWT base64url (RFC 4648 §5).
 *
 * Pourquoi une lib dédiée ? `window.atob` utilise du base64 STANDARD :
 * elle ne gère PAS les caractères `-` et `_` du base64url utilisés par les JWT
 * (les payloads sont deschaînes base64url). Ah si on laisse atob décoder tel quel,
 * on obtient garbage pour les tokens signés avec une clé contenant -/_.
 *
 * Module PUR (aucune dépendance Angular) → testable en vitest sans TestBed.
 */
export function decoderSegmentJwt(segment: string): unknown {
  return JSON.parse(b64UrlToBase64(segment));
}

/** base64url → base64 standard : remplace -/_ puis re-pad to multiple de 4. */
function b64UrlToBase64(segment: string): string {
  let s = segment.replace(/-/g, '+').replace(/_/g, '/');
  const pad = s.length % 4;
  if (pad === 2) s += '==';
  else if (pad === 3) s += '=';
  return s;
}

/** Renvoie la clé `id` numérique d'un payload JWT (ou 0 si absente). */
export function extraireId(payload: unknown): number {
  if (payload && typeof payload === 'object' && 'id' in payload) {
    const id = (payload as Record<string, unknown>)['id'];
    return typeof id === 'number' ? id : Number(id) || 0;
  }
  return 0;
}

/** Renvoie la clé `role` d'un payload JWT (ou null). */
export function extraireRole(payload: unknown): string | null {
  if (payload && typeof payload === 'object' && 'role' in payload) {
    const r = (payload as Record<string, unknown>)['role'];
    return typeof r === 'string' ? r : null;
  }
  return null;
}
