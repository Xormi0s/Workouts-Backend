package com.xormios.workouts.common.seeder;

import com.xormios.workouts.common.entity.workout.Exercise;
import com.xormios.workouts.common.entity.workout.Workout;
import com.xormios.workouts.common.entity.workout.WorkoutDay;
import com.xormios.workouts.common.entity.workout.WorkoutExercise;
import com.xormios.workouts.common.repository.workout.ExerciseRepository;
import com.xormios.workouts.common.repository.workout.WorkoutDayRepository;
import com.xormios.workouts.common.repository.workout.WorkoutExerciseRepository;
import com.xormios.workouts.common.repository.workout.WorkoutRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the reference catalog (exercises, workouts, days, and their exercise slots) that
 * users pick from — they never create these themselves.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class WorkoutSeeder implements Seeder {

    private final ExerciseRepository exerciseRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;

    @Override
    public void seed() {
        Exercise benchPress = findOrCreateExercise("Bench Press");
        Exercise inclineDumbbellPress = findOrCreateExercise("Incline Dumbbell Press");
        Exercise machineChestPress = findOrCreateExercise("Machine Chest Press");
        Exercise overheadPress = findOrCreateExercise("Overhead Press");
        Exercise tricepPushdown = findOrCreateExercise("Tricep Pushdown");

        Exercise deadlift = findOrCreateExercise("Deadlift");
        Exercise barbellRow = findOrCreateExercise("Barbell Row");
        Exercise seatedCableRow = findOrCreateExercise("Seated Cable Row");
        Exercise latPulldown = findOrCreateExercise("Lat Pulldown");
        Exercise bicepCurl = findOrCreateExercise("Bicep Curl");

        Exercise squat = findOrCreateExercise("Squat");
        Exercise legPress = findOrCreateExercise("Leg Press");
        Exercise romanianDeadlift = findOrCreateExercise("Romanian Deadlift");
        Exercise legCurl = findOrCreateExercise("Leg Curl");
        Exercise calfRaise = findOrCreateExercise("Calf Raise");

        Exercise plank = findOrCreateExercise("Plank");

        Workout pushPullLegs = findOrCreateWorkout("Push Pull Legs", "Classic 3-day push/pull/legs split");

        WorkoutDay pushDay = findOrCreateDay(pushPullLegs, "Push Day", 0);
        findOrCreateSlot(pushDay, benchPress, 0, List.of(inclineDumbbellPress, machineChestPress));
        findOrCreateSlot(pushDay, overheadPress, 1, List.of());
        findOrCreateSlot(pushDay, tricepPushdown, 2, List.of());

        WorkoutDay pullDay = findOrCreateDay(pushPullLegs, "Pull Day", 1);
        findOrCreateSlot(pullDay, deadlift, 0, List.of());
        findOrCreateSlot(pullDay, barbellRow, 1, List.of(seatedCableRow));
        findOrCreateSlot(pullDay, latPulldown, 2, List.of());
        findOrCreateSlot(pullDay, bicepCurl, 3, List.of());

        WorkoutDay legDay = findOrCreateDay(pushPullLegs, "Leg Day", 2);
        findOrCreateSlot(legDay, squat, 0, List.of(legPress));
        findOrCreateSlot(legDay, romanianDeadlift, 1, List.of());
        findOrCreateSlot(legDay, legCurl, 2, List.of());
        findOrCreateSlot(legDay, calfRaise, 3, List.of());

        Workout fullBody = findOrCreateWorkout("Full Body", "3-day full body split with slight variation each day");

        WorkoutDay day1 = findOrCreateDay(fullBody, "Day 1", 0);
        findOrCreateSlot(day1, squat, 0, List.of(legPress));
        findOrCreateSlot(day1, benchPress, 1, List.of(inclineDumbbellPress));
        findOrCreateSlot(day1, barbellRow, 2, List.of(seatedCableRow));
        findOrCreateSlot(day1, plank, 3, List.of());

        WorkoutDay day2 = findOrCreateDay(fullBody, "Day 2", 1);
        findOrCreateSlot(day2, deadlift, 0, List.of());
        findOrCreateSlot(day2, overheadPress, 1, List.of());
        findOrCreateSlot(day2, latPulldown, 2, List.of());
        findOrCreateSlot(day2, plank, 3, List.of());

        WorkoutDay day3 = findOrCreateDay(fullBody, "Day 3", 2);
        findOrCreateSlot(day3, legPress, 0, List.of(squat));
        findOrCreateSlot(day3, inclineDumbbellPress, 1, List.of(machineChestPress));
        findOrCreateSlot(day3, seatedCableRow, 2, List.of());
        findOrCreateSlot(day3, plank, 3, List.of());
    }

    private Exercise findOrCreateExercise(String name) {
        return exerciseRepository.findByName(name)
                .orElseGet(() -> exerciseRepository.save(new Exercise(null, name, null)));
    }

    private Workout findOrCreateWorkout(String name, String description) {
        return workoutRepository.findByName(name)
                .orElseGet(() -> workoutRepository.save(new Workout(null, name, description)));
    }

    private WorkoutDay findOrCreateDay(Workout workout, String name, int orderIndex) {
        return workoutDayRepository.findByWorkoutAndName(workout, name)
                .orElseGet(() -> workoutDayRepository.save(new WorkoutDay(null, workout, name, orderIndex)));
    }

    private void findOrCreateSlot(WorkoutDay day, Exercise exercise, int orderIndex, List<Exercise> alternatives) {
        WorkoutExercise slot = workoutExerciseRepository.findByWorkoutDayAndExercise(day, exercise)
                .orElseGet(() -> {
                    WorkoutExercise newSlot = new WorkoutExercise();
                    newSlot.setWorkoutDay(day);
                    newSlot.setExercise(exercise);
                    newSlot.setOrderIndex(orderIndex);
                    return workoutExerciseRepository.save(newSlot);
                });

        if (!alternatives.isEmpty() && slot.getAlternatives().isEmpty()) {
            slot.getAlternatives().addAll(alternatives);
            workoutExerciseRepository.save(slot);
        }
    }
}
