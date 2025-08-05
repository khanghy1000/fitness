package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.WorkoutPlan;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TraineeWorkoutPlanAdapter extends RecyclerView.Adapter<TraineeWorkoutPlanAdapter.TraineeWorkoutPlanViewHolder> {
    private List<WorkoutPlan> workoutPlans = new ArrayList<>();
    private OnWorkoutPlanClickListener listener;

    public interface OnWorkoutPlanClickListener {
        void onWorkoutPlanClick(WorkoutPlan workoutPlan);
    }

    public TraineeWorkoutPlanAdapter(OnWorkoutPlanClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TraineeWorkoutPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trainee_workout_plan, parent, false);
        return new TraineeWorkoutPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TraineeWorkoutPlanViewHolder holder, int position) {
        WorkoutPlan workoutPlan = workoutPlans.get(position);
        holder.bind(workoutPlan, listener);
    }

    @Override
    public int getItemCount() {
        return workoutPlans.size();
    }

    public void updateWorkoutPlans(List<WorkoutPlan> workoutPlans) {
        this.workoutPlans.clear();
        this.workoutPlans.addAll(workoutPlans);
        notifyDataSetChanged();
    }

    public static class TraineeWorkoutPlanViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewWorkoutPlanName;
        private TextView textViewWorkoutPlanDescription;
        private TextView textViewDifficulty;
        private TextView textViewEstimatedCalories;
        private TextView textViewCreatedDate;
        private Chip chipStatus;

        public TraineeWorkoutPlanViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewWorkoutPlanName = itemView.findViewById(R.id.textViewWorkoutPlanName);
            textViewWorkoutPlanDescription = itemView.findViewById(R.id.textViewWorkoutPlanDescription);
            textViewDifficulty = itemView.findViewById(R.id.textViewDifficulty);
            textViewEstimatedCalories = itemView.findViewById(R.id.textViewEstimatedCalories);
            textViewCreatedDate = itemView.findViewById(R.id.textViewCreatedDate);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }

        public void bind(WorkoutPlan workoutPlan, OnWorkoutPlanClickListener listener) {
            textViewWorkoutPlanName.setText(workoutPlan.getName());
            
            if (workoutPlan.getDescription() != null && !workoutPlan.getDescription().isEmpty()) {
                textViewWorkoutPlanDescription.setText(workoutPlan.getDescription());
                textViewWorkoutPlanDescription.setVisibility(View.VISIBLE);
            } else {
                textViewWorkoutPlanDescription.setVisibility(View.GONE);
            }

            // Difficulty
            if (workoutPlan.getDifficulty() != null) {
                String difficulty = workoutPlan.getDifficulty().getValue();
                textViewDifficulty.setText(difficulty.substring(0, 1).toUpperCase() + difficulty.substring(1));
                textViewDifficulty.setVisibility(View.VISIBLE);
            } else {
                textViewDifficulty.setVisibility(View.GONE);
            }

            // Estimated calories
            if (workoutPlan.getEstimatedCalories() != null) {
                textViewEstimatedCalories.setText("~" + workoutPlan.getEstimatedCalories() + " cal");
                textViewEstimatedCalories.setVisibility(View.VISIBLE);
            } else {
                textViewEstimatedCalories.setVisibility(View.GONE);
            }

            // Created date
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(workoutPlan.getCreatedAt());
                textViewCreatedDate.setText("Created: " + outputFormat.format(date));
            } catch (Exception e) {
                textViewCreatedDate.setText("Created: " + workoutPlan.getCreatedAt());
            }

            // Status
            chipStatus.setText(workoutPlan.isActive() ? "Active" : "Inactive");
            chipStatus.setChecked(workoutPlan.isActive());

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWorkoutPlanClick(workoutPlan);
                }
            });
        }
    }
}
