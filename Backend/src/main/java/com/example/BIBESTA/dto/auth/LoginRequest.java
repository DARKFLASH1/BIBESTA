package com.example.BIBESTA.dto.auth;

public record LoginRequest(
        String identifiant,
        String motDePasse) {
}