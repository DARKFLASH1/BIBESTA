package com.example.BIBESTA.exception;

// Conflit d'état métier (ex : "Une amende existe déjà pour cet emprunt",
// "Pas de retard détecté"). Mappé en HTTP 409 Conflict par GlobalExceptionHandler.
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}