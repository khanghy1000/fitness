package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.databinding.ItemNutritionMealEditBinding;
import com.example.fitness.ui.viewmodel.NutritionPlanEditViewModel;

import java.util.ArrayList;
import java.util.List;

public class NutritionMealEditAdapter extends RecyclerView.Adapter<NutritionMealEditAdapter.MealEditViewHolder> {
    private List<NutritionPlanEditViewModel.EditablePlanMeal> meals = new ArrayList<>();
    private final int dayIndex;
    private final OnMealActionListener mealActionListener;

    public interface OnMealActionListener {
        void onAddFood(int dayIndex, int mealIndex);
        void onRemoveFood(int dayIndex, int mealIndex, int foodIndex);
        void onRemoveMeal(int dayIndex, int mealIndex);
    }

    public NutritionMealEditAdapter(int dayIndex, OnMealActionListener mealActionListener) {
        this.dayIndex = dayIndex;
        this.mealActionListener = mealActionListener;
    }

    @NonNull
    @Override
    public MealEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNutritionMealEditBinding binding = ItemNutritionMealEditBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MealEditViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MealEditViewHolder holder, int position) {
        NutritionPlanEditViewModel.EditablePlanMeal meal = meals.get(position);
        holder.bind(meal, dayIndex, position, mealActionListener);
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public void updateMeals(List<NutritionPlanEditViewModel.EditablePlanMeal> meals) {
        this.meals.clear();
        this.meals.addAll(meals);
        notifyDataSetChanged();
    }

    public static class MealEditViewHolder extends RecyclerView.ViewHolder {
        private final ItemNutritionMealEditBinding binding;
        private NutritionFoodEditAdapter foodAdapter;

        public MealEditViewHolder(ItemNutritionMealEditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            binding.recyclerViewEditFoods.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        }

        public void bind(NutritionPlanEditViewModel.EditablePlanMeal meal, int dayIndex, int mealIndex, OnMealActionListener listener) {
            // Set meal data
            binding.editTextMealName.setText(meal.name);
            binding.editTextMealTime.setText(meal.time);
            binding.editTextMealCalories.setText(meal.calories);
            binding.editTextMealProtein.setText(meal.protein);
            binding.editTextMealCarbs.setText(meal.carbs);
            binding.editTextMealFat.setText(meal.fat);

            // Setup food adapter
            foodAdapter = new NutritionFoodEditAdapter(dayIndex, mealIndex, new NutritionFoodEditAdapter.OnFoodActionListener() {
                @Override
                public void onRemoveFood(int dayIndex, int mealIndex, int foodIndex) {
                    listener.onRemoveFood(dayIndex, mealIndex, foodIndex);
                }
            });
            binding.recyclerViewEditFoods.setAdapter(foodAdapter);
            foodAdapter.updateFoods(meal.foods);

            // Set button listeners
            binding.buttonAddFood.setOnClickListener(v -> listener.onAddFood(dayIndex, mealIndex));
            binding.buttonDeleteMeal.setOnClickListener(v -> listener.onRemoveMeal(dayIndex, mealIndex));

            // Setup text change listeners to update the model
            setupTextChangeListeners(meal);
        }

        private void setupTextChangeListeners(NutritionPlanEditViewModel.EditablePlanMeal meal) {
            // Add text watchers to update the meal object when text changes
            // This is a simplified version - in a production app you'd want to use proper text watchers
        }
    }
}
