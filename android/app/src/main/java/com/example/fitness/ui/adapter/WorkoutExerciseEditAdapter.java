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
import com.example.fitness.data.network.model.generated.ExerciseType;
import com.example.fitness.data.repository.ExercisesRepository;
import com.example.fitness.ui.viewmodel.WorkoutPlanEditViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class WorkoutExerciseEditAdapter extends RecyclerView.Adapter<WorkoutExerciseEditAdapter.ExerciseEditViewHolder> {
    private List<WorkoutPlanEditViewModel.EditablePlanExercise> exercises = new ArrayList<>();
    private OnExerciseActionListener listener;
    private int dayIndex;
    private ExercisesRepository exercisesRepository;

    public interface OnExerciseActionListener {
        void onEditExercise(int exerciseIndex);
        void onRemoveExercise(int exerciseIndex);
    }

    @Inject
    public WorkoutExerciseEditAdapter() {
        // Constructor injection will be handled by Dagger if needed
    }

    public void setExercisesRepository(ExercisesRepository exercisesRepository) {
        this.exercisesRepository = exercisesRepository;
    }

    public void setOnExerciseActionListener(OnExerciseActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExerciseEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_exercise_edit, parent, false);
        return new ExerciseEditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseEditViewHolder holder, int position) {
        WorkoutPlanEditViewModel.EditablePlanExercise exercise = exercises.get(position);
        holder.bind(exercise, position, listener, exercisesRepository);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void updateExercises(List<WorkoutPlanEditViewModel.EditablePlanExercise> exercises, int dayIndex) {
        this.exercises.clear();
        this.exercises.addAll(exercises);
        this.dayIndex = dayIndex;
        notifyDataSetChanged();
    }

    public static class ExerciseEditViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewExercise;
        private TextView textViewExerciseName;
        private TextView textViewTargetReps;
        private TextView textViewTargetDuration;
        private TextView textViewSeparator;
        private TextView textViewOrder;
        private TextView textViewNotes;
        private MaterialButton buttonEditExercise;
        private MaterialButton buttonRemoveExercise;

        public ExerciseEditViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewExercise = itemView.findViewById(R.id.imageViewExercise);
            textViewExerciseName = itemView.findViewById(R.id.textViewExerciseName);
            textViewTargetReps = itemView.findViewById(R.id.textViewTargetReps);
            textViewTargetDuration = itemView.findViewById(R.id.textViewTargetDuration);
            textViewSeparator = itemView.findViewById(R.id.textViewSeparator);
            textViewOrder = itemView.findViewById(R.id.textViewOrder);
            textViewNotes = itemView.findViewById(R.id.textViewNotes);
            buttonEditExercise = itemView.findViewById(R.id.buttonEditExercise);
            buttonRemoveExercise = itemView.findViewById(R.id.buttonRemoveExercise);
        }

        public void bind(WorkoutPlanEditViewModel.EditablePlanExercise exercise, int exerciseIndex, 
                        OnExerciseActionListener listener, ExercisesRepository exercisesRepository) {
            // Exercise name
            textViewExerciseName.setText(exercise.exerciseTypeName);

            // Exercise image
            if (exercisesRepository != null) {
                int imageResourceId = exercisesRepository.getExerciseImageResourceByName(exercise.exerciseTypeName);
                Glide.with(itemView.getContext())
                        .asGif()
                        .load(imageResourceId)
                        .placeholder(R.drawable.placeholder_exercise)
                        .error(R.drawable.placeholder_exercise)
                        .into(imageViewExercise);
            }

            // Target reps and duration - show based on LogType
            if (exercise.logType == ExerciseType.LogType.reps) {
                // Show only reps for reps exercises
                if (exercise.targetReps != null && exercise.targetReps > 0) {
                    textViewTargetReps.setText(exercise.targetReps + " reps");
                    textViewTargetReps.setVisibility(View.VISIBLE);
                } else {
                    textViewTargetReps.setVisibility(View.GONE);
                }
                textViewTargetDuration.setVisibility(View.GONE);
                textViewSeparator.setVisibility(View.GONE);
            } else if (exercise.logType == ExerciseType.LogType.duration) {
                // Show only duration for duration exercises
                if (exercise.targetDuration != null && exercise.targetDuration > 0) {
                    textViewTargetDuration.setText(exercise.targetDuration + " sec");
                    textViewTargetDuration.setVisibility(View.VISIBLE);
                } else {
                    textViewTargetDuration.setVisibility(View.GONE);
                }
                textViewTargetReps.setVisibility(View.GONE);
                textViewSeparator.setVisibility(View.GONE);
            } else {
                // Fallback to original logic if LogType is null
                boolean hasReps = exercise.targetReps != null && exercise.targetReps > 0;
                boolean hasDuration = exercise.targetDuration != null && exercise.targetDuration > 0;

                if (hasReps) {
                    textViewTargetReps.setText(exercise.targetReps + " reps");
                    textViewTargetReps.setVisibility(View.VISIBLE);
                } else {
                    textViewTargetReps.setVisibility(View.GONE);
                }

                if (hasDuration) {
                    textViewTargetDuration.setText(exercise.targetDuration + " sec");
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

            // Order - hide as requested
            textViewOrder.setVisibility(View.GONE);

            // Notes
            if (exercise.notes != null && !exercise.notes.isEmpty()) {
                textViewNotes.setText(exercise.notes);
                textViewNotes.setVisibility(View.VISIBLE);
            } else {
                textViewNotes.setVisibility(View.GONE);
            }

            // Set up listeners
            buttonEditExercise.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditExercise(exerciseIndex);
                }
            });

            buttonRemoveExercise.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveExercise(exerciseIndex);
                }
            });
        }
    }
}
