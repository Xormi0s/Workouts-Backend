package com.xormios.workouts.common.repository.workout;

import com.xormios.workouts.common.entity.workout.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise,Long> {

    Optional<Exercise> findByName(String name);
}
