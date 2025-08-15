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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TraineeNutritionPlanAssignmentAdapter extends RecyclerView.Adapter<TraineeNutritionPlanAssignmentAdapter.AssignmentViewHolder> {
    private List<NutritionPlanAssignment> assignments = new ArrayList<>();
    private List<NutritionPlanAssignment> filteredAssignments = new ArrayList<>();
    private Map<String, String> creatorNames = new HashMap<>();
    private OnAssignmentClickListener listener;
    private String currentUserId;

    public interface OnAssignmentClickListener {
        void onAssignmentClick(NutritionPlanAssignment assignment);
        void onAssignmentEdit(NutritionPlanAssignment assignment);
        void onAssignmentOptions(NutritionPlanAssignment assignment, View anchorView);
    }

    public void setOnAssignmentClickListener(OnAssignmentClickListener listener) {
        this.listener = listener;
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
        NutritionPlanAssignment assignment = filteredAssignments.get(position);
        holder.bind(assignment, listener, creatorNames, currentUserId);
    }

    @Override
    public int getItemCount() {
        return filteredAssignments.size();
    }

    public void updateAssignments(List<NutritionPlanAssignment> assignments) {
        this.assignments.clear();
        this.filteredAssignments.clear();
        if (assignments != null) {
            this.assignments.addAll(assignments);
            this.filteredAssignments.addAll(assignments);
        }
        notifyDataSetChanged();
    }

    public void filterByStatus(String status) {
        filteredAssignments.clear();
        if (status.equals("All")) {
            filteredAssignments.addAll(assignments);
        } else {
            for (NutritionPlanAssignment assignment : assignments) {
                if (assignment.getStatus().getValue().equalsIgnoreCase(status)) {
                    filteredAssignments.add(assignment);
                }
            }
        }
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

    public static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewPlanName;
        private TextView textViewPlanDescription;
        private TextView tvStatus;
        private TextView textViewStartDate;
        private TextView textViewCreatedBy;
        private androidx.cardview.widget.CardView cardView;
        private android.widget.ImageButton buttonOptions;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPlanName = itemView.findViewById(R.id.textViewPlanName);
            textViewPlanDescription = itemView.findViewById(R.id.textViewPlanDescription);
            tvStatus = itemView.findViewById(R.id.tv_status);
            textViewStartDate = itemView.findViewById(R.id.textViewStartDate);
            textViewCreatedBy = itemView.findViewById(R.id.textViewCreatedBy);
            cardView = itemView.findViewById(R.id.card_view);
            buttonOptions = itemView.findViewById(R.id.buttonOptions);
        }

        public void bind(NutritionPlanAssignment assignment, OnAssignmentClickListener listener, Map<String, String> creatorNames, String currentUserId) {
            // Set plan name
            if (assignment.getNutritionPlan() != null) {
                textViewPlanName.setText(assignment.getNutritionPlan().getName());
                
                // Handle description visibility
                String description = assignment.getNutritionPlan().getDescription();
                if (description != null && !description.trim().isEmpty()) {
                    textViewPlanDescription.setText(description);
                    textViewPlanDescription.setVisibility(View.VISIBLE);
                } else {
                    textViewPlanDescription.setVisibility(View.GONE);
                }
            } else {
                textViewPlanName.setText("Nutrition Plan #" + assignment.getNutritionPlanId());
                textViewPlanDescription.setVisibility(View.GONE);
            }

            // Set status
            tvStatus.setText("Status: " + assignment.getStatus().getValue());

            // Format start date
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(assignment.getStartDate());
                textViewStartDate.setText("Started: " + outputFormat.format(date));
            } catch (Exception e) {
                textViewStartDate.setText("Started: " + assignment.getStartDate());
            }

            // Set created by
            if (assignment.getNutritionPlan() != null && assignment.getNutritionPlan().getCreatedBy() != null) {
                String creatorId = assignment.getNutritionPlan().getCreatedBy();
                String creatorName = creatorNames != null ? creatorNames.get(creatorId) : null;
                
                if (creatorName != null && !creatorName.isEmpty()) {
                    textViewCreatedBy.setText("Created by: " + creatorName);
                    textViewCreatedBy.setVisibility(View.VISIBLE);
                } else {
                    textViewCreatedBy.setVisibility(View.GONE);
                }
            } else {
                textViewCreatedBy.setVisibility(View.GONE);
            }

            // Show options button only if current user created the plan OR it's an active assignment (for complete action)
            boolean isCreatedByCurrentUser = assignment.getNutritionPlan() != null && 
                assignment.getNutritionPlan().getCreatedBy() != null && 
                assignment.getNutritionPlan().getCreatedBy().equals(currentUserId);
            
            boolean isActiveAssignment = assignment.getStatus() == NutritionPlanAssignment.Status.active;
            
            buttonOptions.setVisibility((isCreatedByCurrentUser || isActiveAssignment) ? View.VISIBLE : View.GONE);
            
            if (isCreatedByCurrentUser || isActiveAssignment) {
                buttonOptions.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAssignmentOptions(assignment, v);
                    }
                });
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
