package com.xormios.workouts.service;

import com.xormios.workouts.common.entity.ApplicationUser;
import com.xormios.workouts.common.entity.workout.Exercise;
import com.xormios.workouts.common.entity.workout.UserExerciseProgress;
import com.xormios.workouts.common.repository.auth.UserRepository;
import com.xormios.workouts.common.repository.workout.ExerciseRepository;
import com.xormios.workouts.common.repository.workout.UserExerciseProgressRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserExerciseProgressService {

    private final UserExerciseProgressRepository userExerciseProgressRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    public void recordProgress(String username, Long exerciseId, double weight, int reps){
        ApplicationUser applicationUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Username not found: " + username));

        Exercise exercise = this.exerciseRepository.findById(exerciseId).orElseThrow(() -> new EntityNotFoundException("Exercise not found"));
        UserExerciseProgress userExerciseProgress = this.userExerciseProgressRepository.findByApplicationUserAndExercise(applicationUser,exercise).orElseGet( () -> {
            UserExerciseProgress temp = new UserExerciseProgress();
            temp.setExercise(exercise);
            temp.setApplicationUser(applicationUser);

            return temp;
        });

        userExerciseProgress.setLastWeightKg(weight);
        userExerciseProgress.setLastReps(reps);

        userExerciseProgressRepository.save(userExerciseProgress);
    }

    public Optional<UserExerciseProgress> getProgress(String username, Long exerciseId){
        ApplicationUser applicationUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Username not found: " + username));

        Exercise exercise = this.exerciseRepository.findById(exerciseId).orElseThrow(() -> new EntityNotFoundException("Exercise not found"));

        return this.userExerciseProgressRepository.findByApplicationUserAndExercise(applicationUser, exercise);
    }
}
