package com.xormios.workouts.dto;

import com.xormios.workouts.common.entity.workout.UserExerciseProgress;

import java.time.LocalDateTime;

public record ExerciseProgressResponse(
        double lastWeightKg,
        int lastReps,
        LocalDateTime lastPerformedAt) {

    public static ExerciseProgressResponse from(UserExerciseProgress userExerciseProgress) {
        return new ExerciseProgressResponse(userExerciseProgress.getLastWeightKg(), userExerciseProgress.getLastReps(), userExerciseProgress.getLastPerformedAt());
    }
}
