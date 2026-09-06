package com.xormios.workouts.common.entity.workout;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "workout_exercise")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_day_id", nullable = false)
    private WorkoutDay workoutDay;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private int orderIndex;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "workout_exercise_alternative",
            joinColumns = @JoinColumn(name = "workout_exercise_id"),
            inverseJoinColumns = @JoinColumn(name = "alternative_exercise_id")
    )
    private Set<Exercise> alternatives = new HashSet<>();
}