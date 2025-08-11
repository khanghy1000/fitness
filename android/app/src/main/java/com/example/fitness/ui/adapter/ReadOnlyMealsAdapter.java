package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.DetailedNutritionPlanMeal;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReadOnlyMealsAdapter extends RecyclerView.Adapter<ReadOnlyMealsAdapter.MealViewHolder> {
    private List<DetailedNutritionPlanMeal> meals = new ArrayList<>();

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_readonly_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        DetailedNutritionPlanMeal meal = meals.get(position);
        holder.bind(meal);
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public void updateMeals(List<DetailedNutritionPlanMeal> meals) {
        this.meals.clear();
        this.meals.addAll(meals);
        notifyDataSetChanged();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView textViewMealName;
        private TextView textViewMealTime;
        private TextView textViewCalories;
        private TextView textViewMacros;
        private TextView textViewFoods;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            textViewMealName = itemView.findViewById(R.id.textViewMealName);
            textViewMealTime = itemView.findViewById(R.id.textViewMealTime);
            textViewCalories = itemView.findViewById(R.id.textViewCalories);
            textViewMacros = itemView.findViewById(R.id.textViewMacros);
            textViewFoods = itemView.findViewById(R.id.textViewFoods);
        }

        public void bind(DetailedNutritionPlanMeal meal) {
            textViewMealName.setText(meal.getName());
            
            // Format meal time
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                Date time = inputFormat.parse(meal.getTime());
                textViewMealTime.setText(outputFormat.format(time));
            } catch (Exception e) {
                textViewMealTime.setText(meal.getTime());
            }

            // Display calories
            if (meal.getCalories() != null) {
                textViewCalories.setText(meal.getCalories() + " cal");
            } else {
                textViewCalories.setText("- cal");
            }

            // Display macros
            StringBuilder macros = new StringBuilder();
            if (meal.getProtein() != null) {
                macros.append("P: ").append(Math.round(meal.getProtein().doubleValue())).append("g");
            }
            if (meal.getCarbs() != null) {
                if (macros.length() > 0) macros.append(" | ");
                macros.append("C: ").append(Math.round(meal.getCarbs().doubleValue())).append("g");
            }
            if (meal.getFat() != null) {
                if (macros.length() > 0) macros.append(" | ");
                macros.append("F: ").append(Math.round(meal.getFat().doubleValue())).append("g");
            }
            
            if (macros.length() > 0) {
                textViewMacros.setText(macros.toString());
                textViewMacros.setVisibility(View.VISIBLE);
            } else {
                textViewMacros.setVisibility(View.GONE);
            }

            // Display foods with quantities
            if (meal.getFoods() != null && !meal.getFoods().isEmpty()) {
                StringBuilder foodsText = new StringBuilder();
                for (int i = 0; i < meal.getFoods().size(); i++) {
                    if (i > 0) foodsText.append("\n");
                    
                    String foodName = meal.getFoods().get(i).getName();
                    String quantity = meal.getFoods().get(i).getQuantity();
                    
                    foodsText.append("• ").append(foodName);
                    if (quantity != null && !quantity.trim().isEmpty()) {
                        foodsText.append(" (").append(quantity).append(")");
                    }
                }
                textViewFoods.setText(foodsText.toString());
                textViewFoods.setVisibility(View.VISIBLE);
            } else {
                textViewFoods.setVisibility(View.GONE);
            }
        }
    }
}
