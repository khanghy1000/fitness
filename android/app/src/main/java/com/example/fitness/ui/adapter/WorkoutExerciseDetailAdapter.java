package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.DetailedWorkoutPlanDayExercise;
import com.example.fitness.data.network.model.generated.ExerciseType;
import com.example.fitness.data.repository.ExercisesRepository;

import java.util.ArrayList;
import java.util.List;

public class WorkoutExerciseDetailAdapter extends RecyclerView.Adapter<WorkoutExerciseDetailAdapter.ExerciseViewHolder> {
    private List<DetailedWorkoutPlanDayExercise> exercises = new ArrayList<>();
    private ExercisesRepository exercisesRepository;

    public WorkoutExerciseDetailAdapter() {
        // Default constructor
    }

    public void setExercisesRepository(ExercisesRepository exercisesRepository) {
        this.exercisesRepository = exercisesRepository;
        notifyDataSetChanged(); // Refresh to load images
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_exercise_detail, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        DetailedWorkoutPlanDayExercise exercise = exercises.get(position);
        holder.bind(exercise, exercisesRepository);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void updateExercises(List<DetailedWorkoutPlanDayExercise> exercises) {
        this.exercises.clear();
        this.exercises.addAll(exercises);
        notifyDataSetChanged();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewExercise;
        private TextView textViewExerciseName;
        private TextView textViewTargetReps;
        private TextView textViewTargetDuration;
        private TextView textViewSeparator;
        private TextView textViewEstimatedCalories;
        private TextView textViewNotes;
        private TextView textViewOrder;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewExercise = itemView.findViewById(R.id.imageViewExercise);
            textViewExerciseName = itemView.findViewById(R.id.textViewExerciseName);
            textViewTargetReps = itemView.findViewById(R.id.textViewTargetReps);
            textViewTargetDuration = itemView.findViewById(R.id.textViewTargetDuration);
            textViewSeparator = itemView.findViewById(R.id.textViewSeparator);
            textViewEstimatedCalories = itemView.findViewById(R.id.textViewEstimatedCalories);
            textViewNotes = itemView.findViewById(R.id.textViewNotes);
            textViewOrder = itemView.findViewById(R.id.textViewOrder);
        }

        public void bind(DetailedWorkoutPlanDayExercise exercise, ExercisesRepository exercisesRepository) {
            // Exercise name
            textViewExerciseName.setText(exercise.getExerciseType().getName());

            // Exercise image
            if (exercisesRepository != null) {
                int imageResourceId = exercisesRepository.getExerciseImageResourceByName(exercise.getExerciseType().getName());
                Glide.with(itemView.getContext())
                        .asGif()
                        .load(imageResourceId)
                        .placeholder(R.drawable.placeholder_exercise)
                        .error(R.drawable.placeholder_exercise)
                        .into(imageViewExercise);
            }

            // Target reps and duration - show based on LogType
            ExerciseType.LogType logType = exercise.getExerciseType().getLogType();
            if (logType == ExerciseType.LogType.reps) {
                // Show only reps for reps exercises
                if (exercise.getTargetReps() != null && exercise.getTargetReps() > 0) {
                    textViewTargetReps.setText(exercise.getTargetReps() + " reps");
                    textViewTargetReps.setVisibility(View.VISIBLE);
                } else {
                    textViewTargetReps.setVisibility(View.GONE);
                }
                textViewTargetDuration.setVisibility(View.GONE);
                textViewSeparator.setVisibility(View.GONE);
            } else if (logType == ExerciseType.LogType.duration) {
                // Show only duration for duration exercises
                if (exercise.getTargetDuration() != null && exercise.getTargetDuration() > 0) {
                    textViewTargetDuration.setText(exercise.getTargetDuration() + " sec");
                    textViewTargetDuration.setVisibility(View.VISIBLE);
                } else {
                    textViewTargetDuration.setVisibility(View.GONE);
                }
                textViewTargetReps.setVisibility(View.GONE);
                textViewSeparator.setVisibility(View.GONE);
            } else {
                // Fallback to original logic if LogType is null
                boolean hasReps = exercise.getTargetReps() != null && exercise.getTargetReps() > 0;
                boolean hasDuration = exercise.getTargetDuration() != null && exercise.getTargetDuration() > 0;

                if (hasReps) {
                    textViewTargetReps.setText(exercise.getTargetReps() + " reps");
                    textViewTargetReps.setVisibility(View.VISIBLE);
                } else {
                    textViewTargetReps.setVisibility(View.GONE);
                }

                if (hasDuration) {
                    textViewTargetDuration.setText(exercise.getTargetDuration() + " sec");
                    textViewTargetDuration.setVisibility(View.VISIBLE);
                } else {
                    textViewTargetDuration.setVisibility(View.GONE);
                }

                // Separator
                if (hasReps && hasDuration) {
                    textViewSeparator.setVisibility(View.VISIBLE);
                } else {
                    textViewSeparator.setVisibility(View.GONE);
                }
            }

            // Estimated calories
            if (exercise.getEstimatedCalories() != null) {
                textViewEstimatedCalories.setText("~" + exercise.getEstimatedCalories() + " cal");
                textViewEstimatedCalories.setVisibility(View.VISIBLE);
            } else {
                textViewEstimatedCalories.setVisibility(View.GONE);
            }

            // Notes
            if (exercise.getNotes() != null && !exercise.getNotes().isEmpty()) {
                textViewNotes.setText(exercise.getNotes());
                textViewNotes.setVisibility(View.VISIBLE);
            } else {
                textViewNotes.setVisibility(View.GONE);
            }

            // Order - hide as requested
            textViewOrder.setVisibility(View.GONE);
        }
    }
}
