package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.DetailedWorkoutPlanDayExercise;
import com.example.fitness.data.network.model.generated.ExerciseType;
import com.example.fitness.data.network.model.generated.WorkoutPlanResults;
import com.example.fitness.data.network.model.generated.WorkoutPlanResultsWorkoutPlanDaysInner;
import com.example.fitness.data.network.model.generated.WorkoutPlanResultsWorkoutPlanDaysInnerExercisesInner;
import com.example.fitness.data.repository.ExercisesRepository;
import com.example.fitness.databinding.ItemTraineeWorkoutExerciseBinding;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.scopes.ActivityScoped;

@ActivityScoped
public class TraineeWorkoutExerciseAdapter extends RecyclerView.Adapter<TraineeWorkoutExerciseAdapter.ExerciseViewHolder> {
    private List<DetailedWorkoutPlanDayExercise> exercises = new ArrayList<>();
    private ExercisesRepository exercisesRepository;
    private WorkoutPlanResults workoutPlanResults;
    private int currentDayNumber;

    @Inject
    public TraineeWorkoutExerciseAdapter() {
    }

    public void setExercisesRepository(ExercisesRepository exercisesRepository) {
        this.exercisesRepository = exercisesRepository;
    }

    public void setWorkoutPlanResults(WorkoutPlanResults results, int dayNumber) {
        this.workoutPlanResults = results;
        this.currentDayNumber = dayNumber;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTraineeWorkoutExerciseBinding binding = ItemTraineeWorkoutExerciseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ExerciseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        DetailedWorkoutPlanDayExercise exercise = exercises.get(position);
        holder.bind(exercise);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void updateExercises(List<DetailedWorkoutPlanDayExercise> newExercises) {
        this.exercises.clear();
        if (newExercises != null) {
            this.exercises.addAll(newExercises);
        }
        notifyDataSetChanged();
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private final ItemTraineeWorkoutExerciseBinding binding;

        public ExerciseViewHolder(ItemTraineeWorkoutExerciseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DetailedWorkoutPlanDayExercise exercise) {
            // Exercise name
            binding.textViewExerciseName.setText(exercise.getExerciseType().getName());
            
            // Exercise image with Glide for GIF support
            if (exercisesRepository != null) {
                int imageResource = exercisesRepository.getExerciseImageResourceByName(exercise.getExerciseType().getName());
                Glide.with(binding.getRoot().getContext())
                        .asGif()
                        .load(imageResource)
                        .placeholder(R.drawable.placeholder_exercise)
                        .error(R.drawable.placeholder_exercise)
                        .into(binding.imageViewExercise);
            }
            
            // Duration/Reps based on log type
            ExerciseType.LogType logType = exercise.getExerciseType().getLogType();
            if (logType == ExerciseType.LogType.duration) {
                // Show duration
                int duration = exercise.getTargetDuration() != null ? exercise.getTargetDuration() : 0;
                binding.textViewDurationReps.setText(duration + " sec");
            } else if (logType == ExerciseType.LogType.reps) {
                // Show reps
                int reps = exercise.getTargetReps() != null ? exercise.getTargetReps() : 0;
                binding.textViewDurationReps.setText(reps + " reps");
            } else {
                binding.textViewDurationReps.setText("-");
            }
            
            // Notes (if available)
            if (exercise.getNotes() != null && !exercise.getNotes().isEmpty()) {
                binding.textViewNotes.setText(exercise.getNotes());
                binding.textViewNotes.setVisibility(View.VISIBLE);
            } else {
                binding.textViewNotes.setVisibility(View.GONE);
            }
            
            // Show completion status
            showCompletionStatus(exercise);
        }

        private void showCompletionStatus(DetailedWorkoutPlanDayExercise exercise) {
            if (workoutPlanResults == null || workoutPlanResults.getWorkoutPlanDays() == null) {
                binding.imageViewCompletionStatus.setVisibility(View.GONE);
                return;
            }

            // Find the day in results
            WorkoutPlanResultsWorkoutPlanDaysInner dayResults = null;
            for (WorkoutPlanResultsWorkoutPlanDaysInner day : workoutPlanResults.getWorkoutPlanDays()) {
                if (day.getDay() == currentDayNumber) {
                    dayResults = day;
                    break;
                }
            }

            if (dayResults == null || dayResults.getExercises() == null) {
                binding.imageViewCompletionStatus.setVisibility(View.GONE);
                return;
            }

            // Check if this exercise is completed
            boolean isCompleted = false;
            for (WorkoutPlanResultsWorkoutPlanDaysInnerExercisesInner exerciseResult : dayResults.getExercises()) {
                if (exerciseResult.getId() == (exercise.getId()) &&
                    exerciseResult.getExerciseResults() != null && 
                    !exerciseResult.getExerciseResults().isEmpty()) {
                    isCompleted = true;
                    break;
                }
            }

            if (isCompleted) {
                binding.imageViewCompletionStatus.setVisibility(View.VISIBLE);
                binding.imageViewCompletionStatus.setImageResource(R.drawable.ic_check_circle);
            } else {
                binding.imageViewCompletionStatus.setVisibility(View.GONE);
            }
        }
    }
}
