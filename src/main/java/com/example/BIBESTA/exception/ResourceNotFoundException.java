package com.example.BIBESTA.exception;

// Exception quand une ressource n'est pas trouvée
// Ex: "Livre non trouvé avec l'id 99"
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}