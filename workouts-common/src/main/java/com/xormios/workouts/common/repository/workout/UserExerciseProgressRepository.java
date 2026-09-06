package com.xormios.workouts.common.repository.workout;

import com.xormios.workouts.common.entity.ApplicationUser;
import com.xormios.workouts.common.entity.workout.Exercise;
import com.xormios.workouts.common.entity.workout.UserExerciseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserExerciseProgressRepository extends JpaRepository<UserExerciseProgress, Long> {

    Optional<UserExerciseProgress> findByApplicationUserAndExercise(ApplicationUser applicationUser, Exercise exercise);
}