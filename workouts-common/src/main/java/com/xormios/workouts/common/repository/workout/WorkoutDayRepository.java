package com.xormios.workouts.common.repository.workout;

import com.xormios.workouts.common.entity.workout.Workout;
import com.xormios.workouts.common.entity.workout.WorkoutDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutDayRepository extends JpaRepository<WorkoutDay, Long> {

    List<WorkoutDay> findByWorkoutIdOrderByOrderIndex(Long workoutId);

    Optional<WorkoutDay> findByWorkoutAndName(Workout workout, String name);
}
