package com.xormios.workouts.common.repository.workout;

import com.xormios.workouts.common.entity.workout.Exercise;
import com.xormios.workouts.common.entity.workout.WorkoutDay;
import com.xormios.workouts.common.entity.workout.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {

    @Query("SELECT we FROM WorkoutExercise we " +
            "JOIN FETCH we.exercise " +
            "LEFT JOIN FETCH we.alternatives " +
            "WHERE we.workoutDay.id = :workoutDayId " +
            "ORDER BY we.orderIndex")
    List<WorkoutExercise> findByWorkoutDayIdOrderByOrderIndex(@Param("workoutDayId") Long workoutDayId);

    Optional<WorkoutExercise> findByWorkoutDayAndExercise(WorkoutDay workoutDay, Exercise exercise);
}
