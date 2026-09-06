package com.xormios.workouts.common.repository.workout;

import com.xormios.workouts.common.entity.ApplicationUser;
import com.xormios.workouts.common.entity.workout.UserWorkoutExerciseChoice;
import com.xormios.workouts.common.entity.workout.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserWorkoutExerciseChoiceRepository extends JpaRepository<UserWorkoutExerciseChoice, Long> {

    Optional<UserWorkoutExerciseChoice> findByApplicationUserAndWorkoutExercise(ApplicationUser applicationUser, WorkoutExercise workoutExercise);

    List<UserWorkoutExerciseChoice> findByApplicationUser_IdAndWorkoutExercise_WorkoutDay_Id(Long userId, Long workoutDayId);
}