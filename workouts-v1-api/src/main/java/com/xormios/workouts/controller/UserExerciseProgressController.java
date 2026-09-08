package com.xormios.workouts.controller;

import com.xormios.workouts.dto.ExerciseProgressResponse;
import com.xormios.workouts.dto.RecordProgressRequest;
import com.xormios.workouts.service.UserExerciseProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
@Slf4j
public class UserExerciseProgressController {

    private final UserExerciseProgressService userExerciseProgressService;

    @PostMapping("/{exerciseId}/progress")
    public ResponseEntity<Void> recordProgress(@PathVariable Long exerciseId, @Valid @RequestBody RecordProgressRequest recordProgressRequest, Authentication authentication) {
        log.info("Recording progress for exercise {} and user {}", exerciseId, authentication.getName());

        this.userExerciseProgressService.recordProgress(authentication.getName(), exerciseId, recordProgressRequest.weightKg(), recordProgressRequest.reps());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{exerciseId}/progress")
    public ResponseEntity<ExerciseProgressResponse> getProgress(@PathVariable Long exerciseId, Authentication authentication) {
        log.info("Getting progress for exercise {} and user {}", exerciseId, authentication.getName());

        return userExerciseProgressService.getProgress(authentication.getName(), exerciseId).map(ExerciseProgressResponse::from).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}
