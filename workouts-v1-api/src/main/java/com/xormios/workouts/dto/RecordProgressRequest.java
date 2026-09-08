package com.xormios.workouts.dto;

import jakarta.validation.constraints.Positive;

public record RecordProgressRequest(
        @Positive double weightKg,
        @Positive int reps) {}
