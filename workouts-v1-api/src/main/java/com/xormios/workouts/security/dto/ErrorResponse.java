package com.xormios.workouts.security.dto;

public record ErrorResponse(
        String error,
        String message) {}
