package com.xormios.workouts.common.repository.workout;

import com.xormios.workouts.common.entity.workout.WorkoutDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutDayRepository extends JpaRepository<WorkoutDay, Long> {

    List<WorkoutDay> findByWorkoutIdOrderByOrderIndex(Long workoutId);
}
