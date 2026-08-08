package com.xormios.workouts.security.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds) {}
