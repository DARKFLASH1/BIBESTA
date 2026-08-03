package com.example.BIBESTA.exception;

import java.time.LocalDateTime;

// Structure standard de toutes les erreurs renvoyées à Angular
public record ApiError(
        int status, // code HTTP (400, 401, 403, 404, 500)
        String message, // message lisible par l'utilisateur
        LocalDateTime timestamp // quand l'erreur s'est produite
) {
}