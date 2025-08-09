package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.NutritionPlanAssignment;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TraineeNutritionPlanAssignmentAdapter extends RecyclerView.Adapter<TraineeNutritionPlanAssignmentAdapter.AssignmentViewHolder> {
    private List<NutritionPlanAssignment> assignments = new ArrayList<>();
    private OnAssignmentClickListener listener;

    public interface OnAssignmentClickListener {
        void onAssignmentClick(NutritionPlanAssignment assignment);
    }

    public TraineeNutritionPlanAssignmentAdapter(OnAssignmentClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trainee_nutrition_plan_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        NutritionPlanAssignment assignment = assignments.get(position);
        holder.bind(assignment, listener);
    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }

    public void updateAssignments(List<NutritionPlanAssignment> assignments) {
        this.assignments.clear();
        this.assignments.addAll(assignments);
        notifyDataSetChanged();
    }

    public static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewPlanName;
        private TextView textViewPlanDescription;
        private TextView textViewStartDate;
        private TextView textViewAssignedBy;
        private Chip chipStatus;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPlanName = itemView.findViewById(R.id.textViewPlanName);
            textViewPlanDescription = itemView.findViewById(R.id.textViewPlanDescription);
            textViewStartDate = itemView.findViewById(R.id.textViewStartDate);
            textViewAssignedBy = itemView.findViewById(R.id.textViewAssignedBy);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }

        public void bind(NutritionPlanAssignment assignment, OnAssignmentClickListener listener) {
            // Set plan name and description
            if (assignment.getNutritionPlan() != null) {
                textViewPlanName.setText(assignment.getNutritionPlan().getName());
                
                if (assignment.getNutritionPlan().getDescription() != null && 
                    !assignment.getNutritionPlan().getDescription().isEmpty()) {
                    textViewPlanDescription.setText(assignment.getNutritionPlan().getDescription());
                    textViewPlanDescription.setVisibility(View.VISIBLE);
                } else {
                    textViewPlanDescription.setVisibility(View.GONE);
                }
            } else {
                textViewPlanName.setText("Nutrition Plan #" + assignment.getNutritionPlanId());
                textViewPlanDescription.setVisibility(View.GONE);
            }

            // Format start date
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(assignment.getStartDate());
                textViewStartDate.setText("Started: " + outputFormat.format(date));
            } catch (Exception e) {
                textViewStartDate.setText("Started: " + assignment.getStartDate());
            }

            // Set assigned by
            textViewAssignedBy.setText("Assigned by: " + assignment.getAssignedBy());

            // Set status chip
            String statusText = assignment.getStatus().getValue();
            statusText = statusText.substring(0, 1).toUpperCase() + statusText.substring(1);
            chipStatus.setText(statusText);
            
            // Set chip appearance based on status
            switch (assignment.getStatus()) {
                case active:
                    chipStatus.setChipBackgroundColorResource(R.color.green_100);
                    chipStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.green_800, null));
                    break;
                case completed:
                    chipStatus.setChipBackgroundColorResource(R.color.blue_100);
                    chipStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.blue_800, null));
                    break;
                case paused:
                    chipStatus.setChipBackgroundColorResource(R.color.orange_100);
                    chipStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.orange_800, null));
                    break;
                case cancelled:
                    chipStatus.setChipBackgroundColorResource(R.color.red_100);
                    chipStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.red_800, null));
                    break;
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAssignmentClick(assignment);
                }
            });
        }
    }
}
