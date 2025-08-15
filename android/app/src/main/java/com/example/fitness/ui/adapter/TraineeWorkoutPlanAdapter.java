package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.WorkoutPlan;
import com.example.fitness.data.network.model.generated.WorkoutPlanAssignment;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TraineeWorkoutPlanAdapter extends RecyclerView.Adapter<TraineeWorkoutPlanAdapter.TraineeWorkoutPlanViewHolder> {
    private List<WorkoutPlanAssignment> workoutPlanAssignments = new ArrayList<>();
    private Map<String, String> creatorNames = new HashMap<>();
    private OnWorkoutPlanClickListener listener;
    private String currentUserId;

    public interface OnWorkoutPlanClickListener {
        void onWorkoutPlanClick(WorkoutPlan workoutPlan);
        void onWorkoutPlanEdit(WorkoutPlan workoutPlan);
        void onWorkoutPlanDelete(WorkoutPlan workoutPlan);
        void onWorkoutPlanOptionsClick(WorkoutPlan workoutPlan, View anchorView);
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
        WorkoutPlanAssignment workoutPlanAssignment = workoutPlanAssignments.get(position);
        holder.bind(workoutPlanAssignment, listener, creatorNames, currentUserId);
    }

    @Override
    public int getItemCount() {
        return workoutPlanAssignments.size();
    }

    public void updateWorkoutPlanAssignments(List<WorkoutPlanAssignment> workoutPlanAssignments) {
        this.workoutPlanAssignments.clear();
        this.workoutPlanAssignments.addAll(workoutPlanAssignments);
        notifyDataSetChanged();
    }

    public void updateCreatorNames(Map<String, String> creatorNames) {
        if (creatorNames != null) {
            this.creatorNames.clear();
            this.creatorNames.putAll(creatorNames);
            // Always notify all items to refresh their creator names
            notifyDataSetChanged();
        }
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
        notifyDataSetChanged();
    }

    public static class TraineeWorkoutPlanViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewWorkoutPlanName;
        private TextView textViewWorkoutPlanDescription;
        private TextView textViewDifficulty;
        private TextView textViewEstimatedCalories;
        private TextView textViewCreatedDate;
        private TextView textViewProgress;
        private TextView textViewCreatedBy;
        private Chip chipStatus;
        private View buttonOptions;

        public TraineeWorkoutPlanViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewWorkoutPlanName = itemView.findViewById(R.id.textViewWorkoutPlanName);
            textViewWorkoutPlanDescription = itemView.findViewById(R.id.textViewWorkoutPlanDescription);
            textViewDifficulty = itemView.findViewById(R.id.textViewDifficulty);
            textViewEstimatedCalories = itemView.findViewById(R.id.textViewEstimatedCalories);
            textViewCreatedDate = itemView.findViewById(R.id.textViewCreatedDate);
            textViewProgress = itemView.findViewById(R.id.textViewProgress);
            textViewCreatedBy = itemView.findViewById(R.id.textViewCreatedBy);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            buttonOptions = itemView.findViewById(R.id.buttonOptions);
        }

        public void bind(WorkoutPlanAssignment workoutPlanAssignment, OnWorkoutPlanClickListener listener, Map<String, String> creatorNames, String currentUserId) {
            WorkoutPlan workoutPlan = workoutPlanAssignment.getWorkoutPlan();
            
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

            // Progress
            if (workoutPlanAssignment.getProgress() != null && textViewProgress != null) {
                int progressPercent = workoutPlanAssignment.getProgress().intValue();
                textViewProgress.setText("Progress: " + progressPercent + "%");
                textViewProgress.setVisibility(View.VISIBLE);
            } else if (textViewProgress != null) {
                textViewProgress.setVisibility(View.GONE);
            }

            // Set created by
            if (workoutPlan.getCreatedBy() != null) {
                String creatorId = workoutPlan.getCreatedBy();
                String creatorName = creatorNames != null ? creatorNames.get(creatorId) : null;
                
                // Debug logging
                android.util.Log.d("CreatorDebug", "Workout - Creator ID: " + creatorId + ", Creator Name: " + creatorName + ", Map size: " + (creatorNames != null ? creatorNames.size() : "null"));
                
                if (creatorName != null && !creatorName.isEmpty()) {
                    textViewCreatedBy.setText("Created by: " + creatorName);
                    textViewCreatedBy.setVisibility(View.VISIBLE);
                } else {
                    // Hide while loading, will show when data is available
                    textViewCreatedBy.setVisibility(View.GONE);
                }
            } else {
                textViewCreatedBy.setVisibility(View.GONE);
            }

            // Start date instead of created date
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(workoutPlanAssignment.getStartDate());
                textViewCreatedDate.setText("Started: " + outputFormat.format(date));
            } catch (Exception e) {
                textViewCreatedDate.setText("Started: " + workoutPlanAssignment.getStartDate());
            }

            // Status from assignment
            String status = workoutPlanAssignment.getStatus().getValue();
            chipStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
            chipStatus.setChecked(workoutPlanAssignment.getStatus() == WorkoutPlanAssignment.Status.active);

            // Show options button only if current user created this plan
            boolean isCreatedByCurrentUser = workoutPlan.getCreatedBy() != null && 
                                           workoutPlan.getCreatedBy().equals(currentUserId);
            
            if (buttonOptions != null) {
                buttonOptions.setVisibility(isCreatedByCurrentUser ? View.VISIBLE : View.GONE);
                if (isCreatedByCurrentUser) {
                    buttonOptions.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onWorkoutPlanOptionsClick(workoutPlan, v);
                        }
                    });
                }
            }

            // Click listener - pass the workout plan
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWorkoutPlanClick(workoutPlan);
                }
            });
        }
    }
}
