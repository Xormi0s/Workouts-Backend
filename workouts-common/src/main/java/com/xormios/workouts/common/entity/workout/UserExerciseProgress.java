package com.xormios.workouts.common.entity.workout;

import com.xormios.workouts.common.entity.ApplicationUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_exercise_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"application_user_id", "exercise_id"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserExerciseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_user_id", nullable = false)
    private ApplicationUser applicationUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private double lastWeightKg;

    @Column(nullable = false)
    private int lastReps;

    @UpdateTimestamp
    private LocalDateTime lastPerformedAt;
}