package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.WorkoutPlan;

import java.util.ArrayList;
import java.util.List;

public class AssignWorkoutPlanAdapter extends RecyclerView.Adapter<AssignWorkoutPlanAdapter.WorkoutPlanViewHolder> {
    private List<WorkoutPlan> workoutPlans = new ArrayList<>();
    private OnWorkoutPlanAssignListener listener;

    public interface OnWorkoutPlanAssignListener {
        void onWorkoutPlanSelect(String workoutPlanId, String workoutPlanName);
    }

    public AssignWorkoutPlanAdapter(OnWorkoutPlanAssignListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assign_workout_plan, parent, false);
        return new WorkoutPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutPlanViewHolder holder, int position) {
        WorkoutPlan workoutPlan = workoutPlans.get(position);
        holder.bind(workoutPlan, listener);
    }

    @Override
    public int getItemCount() {
        return workoutPlans.size();
    }

    public void updateWorkoutPlans(List<WorkoutPlan> newWorkoutPlans) {
        this.workoutPlans.clear();
        this.workoutPlans.addAll(newWorkoutPlans);
        notifyDataSetChanged();
    }

    static class WorkoutPlanViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvName;
        private TextView tvDescription;
        private TextView tvDifficulty;
        private TextView tvDuration;
        private TextView tvCalories;

        public WorkoutPlanViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDifficulty = itemView.findViewById(R.id.tv_difficulty);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvCalories = itemView.findViewById(R.id.tv_calories);
        }

        public void bind(WorkoutPlan workoutPlan, OnWorkoutPlanAssignListener listener) {
            tvName.setText(workoutPlan.getName());
            
            if (workoutPlan.getDescription() != null && !workoutPlan.getDescription().isEmpty()) {
                tvDescription.setText(workoutPlan.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }
            
            if (workoutPlan.getDifficulty() != null) {
                tvDifficulty.setText("Difficulty: " + workoutPlan.getDifficulty().getValue());
                tvDifficulty.setVisibility(View.VISIBLE);
            } else {
                tvDifficulty.setVisibility(View.GONE);
            }
            
            // Duration field doesn't exist in WorkoutPlan model, hide it
            tvDuration.setVisibility(View.GONE);
            
            if (workoutPlan.getEstimatedCalories() != null) {
                tvCalories.setText("Estimated Calories: " + workoutPlan.getEstimatedCalories());
                tvCalories.setVisibility(View.VISIBLE);
            } else {
                tvCalories.setVisibility(View.GONE);
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWorkoutPlanSelect(String.valueOf(workoutPlan.getId()), workoutPlan.getName());
                }
            });
        }
    }
}
