package com.xormios.workouts.common.entity.workout;

import com.xormios.workouts.common.entity.ApplicationUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_workout_exercise_choice",
        uniqueConstraints = @UniqueConstraint(columnNames = {"application_user_id", "workout_exercise_id"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserWorkoutExerciseChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_user_id", nullable = false)
    private ApplicationUser applicationUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    private WorkoutExercise workoutExercise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chosen_exercise_id", nullable = false)
    private Exercise chosenExercise;
}
