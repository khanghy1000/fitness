package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.NutritionPlan;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NutritionPlanAdapter extends RecyclerView.Adapter<NutritionPlanAdapter.NutritionPlanViewHolder> {
    private List<NutritionPlan> nutritionPlans = new ArrayList<>();
    private OnNutritionPlanClickListener listener;

    public interface OnNutritionPlanClickListener {
        void onNutritionPlanClick(NutritionPlan nutritionPlan);
        void onNutritionPlanOptionsClick(NutritionPlan nutritionPlan, View anchorView);
    }

    public NutritionPlanAdapter(OnNutritionPlanClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NutritionPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nutrition_plan, parent, false);
        return new NutritionPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NutritionPlanViewHolder holder, int position) {
        NutritionPlan nutritionPlan = nutritionPlans.get(position);
        holder.bind(nutritionPlan, listener);
    }

    @Override
    public int getItemCount() {
        return nutritionPlans.size();
    }

    public void updateNutritionPlans(List<NutritionPlan> nutritionPlans) {
        this.nutritionPlans.clear();
        this.nutritionPlans.addAll(nutritionPlans);
        notifyDataSetChanged();
    }

    public static class NutritionPlanViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewNutritionPlanName;
        private TextView textViewNutritionPlanDescription;
        private TextView textViewCreatedDate;
        private Chip chipStatus;
        private MaterialButton buttonOptions;

        public NutritionPlanViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNutritionPlanName = itemView.findViewById(R.id.textViewNutritionPlanName);
            textViewNutritionPlanDescription = itemView.findViewById(R.id.textViewNutritionPlanDescription);
            textViewCreatedDate = itemView.findViewById(R.id.textViewCreatedDate);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            buttonOptions = itemView.findViewById(R.id.buttonOptions);
        }

        public void bind(NutritionPlan nutritionPlan, OnNutritionPlanClickListener listener) {
            textViewNutritionPlanName.setText(nutritionPlan.getName());
            
            if (nutritionPlan.getDescription() != null && !nutritionPlan.getDescription().isEmpty()) {
                textViewNutritionPlanDescription.setText(nutritionPlan.getDescription());
                textViewNutritionPlanDescription.setVisibility(View.VISIBLE);
            } else {
                textViewNutritionPlanDescription.setVisibility(View.GONE);
            }

            // Format created date
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(nutritionPlan.getCreatedAt());
                textViewCreatedDate.setText("Created: " + outputFormat.format(date));
            } catch (Exception e) {
                textViewCreatedDate.setText("Created: " + nutritionPlan.getCreatedAt());
            }

            // Set status chip
            chipStatus.setText(nutritionPlan.isActive() ? "Active" : "Inactive");
            chipStatus.setChecked(nutritionPlan.isActive());

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNutritionPlanClick(nutritionPlan);
                }
            });

            buttonOptions.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNutritionPlanOptionsClick(nutritionPlan, v);
                }
            });
        }
    }
}
