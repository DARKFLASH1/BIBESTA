package com.example.BIBESTA.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.LocalDateTime;

@RestControllerAdvice
// @RestControllerAdvice = intercepte toutes les exceptions
// de tous les Controllers automatiquement
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Gère les ressources non trouvées → 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiError(404, ex.getMessage(), LocalDateTime.now()));
    }

    // Gère les règles métier violées → 400
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(
            BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiError(400, ex.getMessage(), LocalDateTime.now()));
    }

    // Conflit d'état métier (doublon, pas de retard, etc.) → 409
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiError(409, ex.getMessage(), LocalDateTime.now()));
    }

    // Gère les erreurs de validation spring (noms/statuts/ids mal formés) → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .orElse("Requête invalide");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiError(400, message, LocalDateTime.now()));
    }

    // Corps JSON illisible/vide ou paramètre mal type → 400
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> handleNotReadable(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiError(400, "Requête invalide : vérifiez le format des données envoyées",
                        LocalDateTime.now()));
    }

    // Violation de contrainte @Valid sur paramètres/entités → 400
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiError(400, "Données invalides", LocalDateTime.now()));
    }

    // Violation d'intégrité en base (unicité, FK...) → 409
    // On ne renvoie JAMAIS le message SQL brut à l'utilisateur.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(
            DataIntegrityViolationException ex) {
        LOGGER.warn("Violation d'intégrité en base : {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiError(409, "Données en conflit avec les enregistrements existants",
                        LocalDateTime.now()));
    }

    // Accès refusé (Spring Security) → 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiError(403, "Accès refusé à cette ressource", LocalDateTime.now()));
    }

    // Gère les RuntimeException génériques → 400
    // Le message réel est loggé côté serveur, JAMAIS renvoyé tel quel au client
    // (risque de fuite d'informations : SQL, chemins, libellés d'enums...).
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntime(
            RuntimeException ex) {
        LOGGER.warn("Erreur applicative : {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiError(400, "Requête invalide", LocalDateTime.now()));
    }

    // Gère toutes les autres exceptions → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(
            Exception ex) {
        LOGGER.error("Erreur serveur inattendue", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiError(500, "Une erreur interne s'est produite", LocalDateTime.now()));
    }
}