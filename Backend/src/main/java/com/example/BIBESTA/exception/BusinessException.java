package com.example.BIBESTA.exception;

// Exception pour les règles métier violées
// Ex: "Exemplaire déjà emprunté", "Amende déjà payée"
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}