package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.repository.ExercisesRepository;
import com.example.fitness.ui.viewmodel.WorkoutPlanEditViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class WorkoutDayEditAdapter extends RecyclerView.Adapter<WorkoutDayEditAdapter.WorkoutDayEditViewHolder> {
    private List<WorkoutPlanEditViewModel.EditablePlanDay> days = new ArrayList<>();
    private OnDayActionListener listener;
    private ExercisesRepository exercisesRepository;

    public interface OnDayActionListener {
        void onRemoveDay(int dayIndex);
        void onConvertToRestDay(int dayIndex);
        void onConvertToWorkoutDay(int dayIndex);
        void onAddExercise(int dayIndex);
        void onEditExercise(int dayIndex, int exerciseIndex);
        void onRemoveExercise(int dayIndex, int exerciseIndex);
    }

    public WorkoutDayEditAdapter(OnDayActionListener listener) {
        this.listener = listener;
    }

    public void setExercisesRepository(ExercisesRepository exercisesRepository) {
        this.exercisesRepository = exercisesRepository;
        notifyDataSetChanged(); // Refresh to load images
    }

    @NonNull
    @Override
    public WorkoutDayEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_day_edit, parent, false);
        return new WorkoutDayEditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutDayEditViewHolder holder, int position) {
        WorkoutPlanEditViewModel.EditablePlanDay day = days.get(position);
        holder.bind(day, position, listener, exercisesRepository);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public void updateDays(List<WorkoutPlanEditViewModel.EditablePlanDay> days) {
        this.days.clear();
        this.days.addAll(days);
        notifyDataSetChanged();
    }

    public static class WorkoutDayEditViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewDayNumber;
        private TextView textViewRestDay;
        private MaterialButton buttonRemoveDay;
        private LinearLayout layoutRestDay;
        private LinearLayout layoutWorkoutDay;
        private MaterialButton buttonConvertToWorkoutDay;
        private MaterialButton buttonAddExercise;
        private RecyclerView recyclerViewExercises;
        private MaterialButton buttonConvertToRestDay;
        private WorkoutExerciseEditAdapter exerciseAdapter;

        public WorkoutDayEditViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDayNumber = itemView.findViewById(R.id.textViewDayNumber);
            textViewRestDay = itemView.findViewById(R.id.textViewRestDay);
            buttonRemoveDay = itemView.findViewById(R.id.buttonRemoveDay);
            layoutRestDay = itemView.findViewById(R.id.layoutRestDay);
            layoutWorkoutDay = itemView.findViewById(R.id.layoutWorkoutDay);
            buttonConvertToWorkoutDay = itemView.findViewById(R.id.buttonConvertToWorkoutDay);
            buttonAddExercise = itemView.findViewById(R.id.buttonAddExercise);
            recyclerViewExercises = itemView.findViewById(R.id.recyclerViewExercises);
            buttonConvertToRestDay = itemView.findViewById(R.id.buttonConvertToRestDay);

            exerciseAdapter = new WorkoutExerciseEditAdapter();
            recyclerViewExercises.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            recyclerViewExercises.setAdapter(exerciseAdapter);
        }

        public void bind(WorkoutPlanEditViewModel.EditablePlanDay day, int dayIndex, OnDayActionListener listener, ExercisesRepository exercisesRepository) {
            textViewDayNumber.setText("Day " + day.day);

            // Set repository for exercise adapter
            if (exerciseAdapter != null && exercisesRepository != null) {
                exerciseAdapter.setExercisesRepository(exercisesRepository);
            }

            if (day.isRestDay) {
                textViewRestDay.setVisibility(View.VISIBLE);
                layoutRestDay.setVisibility(View.VISIBLE);
                layoutWorkoutDay.setVisibility(View.GONE);
            } else {
                textViewRestDay.setVisibility(View.GONE);
                layoutRestDay.setVisibility(View.GONE);
                layoutWorkoutDay.setVisibility(View.VISIBLE);
                exerciseAdapter.updateExercises(day.exercises, dayIndex);
            }

            // Set up listeners
            buttonRemoveDay.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveDay(dayIndex);
                }
            });

            buttonConvertToWorkoutDay.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConvertToWorkoutDay(dayIndex);
                }
            });

            buttonConvertToRestDay.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConvertToRestDay(dayIndex);
                }
            });

            buttonAddExercise.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddExercise(dayIndex);
                }
            });

            exerciseAdapter.setOnExerciseActionListener(new WorkoutExerciseEditAdapter.OnExerciseActionListener() {
                @Override
                public void onEditExercise(int exerciseIndex) {
                    if (listener != null) {
                        listener.onEditExercise(dayIndex, exerciseIndex);
                    }
                }

                @Override
                public void onRemoveExercise(int exerciseIndex) {
                    if (listener != null) {
                        listener.onRemoveExercise(dayIndex, exerciseIndex);
                    }
                }
            });
        }
    }
}
