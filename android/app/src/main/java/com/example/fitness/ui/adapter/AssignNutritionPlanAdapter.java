package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.NutritionPlan;

import java.util.ArrayList;
import java.util.List;

public class AssignNutritionPlanAdapter extends RecyclerView.Adapter<AssignNutritionPlanAdapter.NutritionPlanViewHolder> {
    private List<NutritionPlan> nutritionPlans = new ArrayList<>();
    private OnNutritionPlanAssignListener listener;

    public interface OnNutritionPlanAssignListener {
        void onNutritionPlanSelect(String nutritionPlanId, String nutritionPlanName);
    }

    public AssignNutritionPlanAdapter(OnNutritionPlanAssignListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NutritionPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assign_nutrition_plan, parent, false);
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

    public void updateNutritionPlans(List<NutritionPlan> newNutritionPlans) {
        this.nutritionPlans.clear();
        this.nutritionPlans.addAll(newNutritionPlans);
        notifyDataSetChanged();
    }

    static class NutritionPlanViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvName;
        private TextView tvDescription;
        private TextView tvCalories;
        private TextView tvProtein;
        private TextView tvCarbs;
        private TextView tvFat;

        public NutritionPlanViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvProtein = itemView.findViewById(R.id.tv_protein);
            tvCarbs = itemView.findViewById(R.id.tv_carbs);
            tvFat = itemView.findViewById(R.id.tv_fat);
        }

        public void bind(NutritionPlan nutritionPlan, OnNutritionPlanAssignListener listener) {
            tvName.setText(nutritionPlan.getName());
            
            if (nutritionPlan.getDescription() != null && !nutritionPlan.getDescription().isEmpty()) {
                tvDescription.setText(nutritionPlan.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }
            
            // Hide nutritional info fields since they're not available in basic NutritionPlan
            tvCalories.setVisibility(View.GONE);
            tvProtein.setVisibility(View.GONE);
            tvCarbs.setVisibility(View.GONE);
            tvFat.setVisibility(View.GONE);

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNutritionPlanSelect(String.valueOf(nutritionPlan.getId()), nutritionPlan.getName());
                }
            });
        }
    }
}
