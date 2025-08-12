package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.WorkoutPlanAssignment;

import java.util.ArrayList;
import java.util.List;

public class TraineeWorkoutPlanAssignmentAdapter extends RecyclerView.Adapter<TraineeWorkoutPlanAssignmentAdapter.WorkoutPlanAssignmentViewHolder> {
    private List<WorkoutPlanAssignment> workoutPlanAssignments = new ArrayList<>();
    private List<WorkoutPlanAssignment> filteredAssignments = new ArrayList<>();

    @NonNull
    @Override
    public WorkoutPlanAssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trainee_workout_plan_assignment, parent, false);
        return new WorkoutPlanAssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutPlanAssignmentViewHolder holder, int position) {
        WorkoutPlanAssignment assignment = filteredAssignments.get(position);
        holder.bind(assignment);
    }

    @Override
    public int getItemCount() {
        return filteredAssignments.size();
    }

    public void updateWorkoutPlans(List<WorkoutPlanAssignment> newAssignments) {
        this.workoutPlanAssignments.clear();
        this.filteredAssignments.clear();
        if (newAssignments != null) {
            this.workoutPlanAssignments.addAll(newAssignments);
            this.filteredAssignments.addAll(newAssignments);
        }
        notifyDataSetChanged();
    }

    public void filterByStatus(String status) {
        filteredAssignments.clear();
        if (status.equals("All")) {
            filteredAssignments.addAll(workoutPlanAssignments);
        } else {
            for (WorkoutPlanAssignment assignment : workoutPlanAssignments) {
                if (assignment.getStatus().getValue().equalsIgnoreCase(status)) {
                    filteredAssignments.add(assignment);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class WorkoutPlanAssignmentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPlanName;
        private TextView tvPlanDescription;
        private TextView tvStatus;
        private TextView tvProgress;
        private TextView tvStartDate;
        private TextView tvEndDate;
        private CardView cardView;

        public WorkoutPlanAssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlanName = itemView.findViewById(R.id.tv_plan_name);
            tvPlanDescription = itemView.findViewById(R.id.tv_plan_description);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvProgress = itemView.findViewById(R.id.tv_progress);
            tvStartDate = itemView.findViewById(R.id.tv_start_date);
            tvEndDate = itemView.findViewById(R.id.tv_end_date);
            cardView = itemView.findViewById(R.id.card_view);
        }

        public void bind(WorkoutPlanAssignment assignment) {
            tvPlanName.setText(assignment.getWorkoutPlan().getName());
            
            // Handle description visibility
            String description = assignment.getWorkoutPlan().getDescription();
            if (description != null && !description.trim().isEmpty()) {
                tvPlanDescription.setText(description);
                tvPlanDescription.setVisibility(View.VISIBLE);
            } else {
                tvPlanDescription.setVisibility(View.GONE);
            }
            
            tvStatus.setText("Status: " + assignment.getStatus().getValue());
            tvProgress.setText("Progress: " + assignment.getProgress() + "%");
            tvStartDate.setText("Started: " + assignment.getStartDate());
            
            if (assignment.getEndDate() != null) {
                tvEndDate.setVisibility(View.VISIBLE);
                tvEndDate.setText("Ended: " + assignment.getEndDate());
            } else {
                tvEndDate.setVisibility(View.GONE);
            }

            // Set different background colors based on status
            switch (assignment.getStatus()) {
                case active:
                    cardView.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.white));
                    break;
                case completed:
                    cardView.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.holo_green_light));
                    break;
                case paused:
                    cardView.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.holo_orange_light));
                    break;
                case cancelled:
                    cardView.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.holo_red_light));
                    break;
            }
        }
    }
}
