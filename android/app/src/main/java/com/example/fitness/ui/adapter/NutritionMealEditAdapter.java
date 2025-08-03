package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.databinding.ItemNutritionMealDisplayBinding;
import com.example.fitness.ui.viewmodel.NutritionPlanEditViewModel;

import java.util.ArrayList;
import java.util.List;

public class NutritionMealEditAdapter extends RecyclerView.Adapter<NutritionMealEditAdapter.MealEditViewHolder> {
    private List<NutritionPlanEditViewModel.EditablePlanMeal> meals = new ArrayList<>();
    private final int dayIndex;
    private final OnMealActionListener mealActionListener;
    private final NutritionPlanEditViewModel viewModel;

    public interface OnMealActionListener {
        void onEditMeal(int dayIndex, int mealIndex);
        void onAddFood(int dayIndex, int mealIndex);
        void onEditFood(int dayIndex, int mealIndex, int foodIndex);
        void onRemoveFood(int dayIndex, int mealIndex, int foodIndex);
        void onRemoveMeal(int dayIndex, int mealIndex);
    }

    public NutritionMealEditAdapter(int dayIndex, OnMealActionListener mealActionListener, NutritionPlanEditViewModel viewModel) {
        this.dayIndex = dayIndex;
        this.mealActionListener = mealActionListener;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public MealEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNutritionMealDisplayBinding binding = ItemNutritionMealDisplayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MealEditViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MealEditViewHolder holder, int position) {
        NutritionPlanEditViewModel.EditablePlanMeal meal = meals.get(position);
        holder.bind(meal, dayIndex, position, mealActionListener, viewModel);
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
        private final ItemNutritionMealDisplayBinding binding;
        private NutritionFoodEditAdapter foodAdapter;

        public MealEditViewHolder(ItemNutritionMealDisplayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            binding.recyclerViewEditFoods.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        }

        public void bind(NutritionPlanEditViewModel.EditablePlanMeal meal, int dayIndex, int mealIndex, OnMealActionListener listener, NutritionPlanEditViewModel viewModel) {
            // Display meal data
            binding.textViewMealName.setText(meal.name != null && !meal.name.isEmpty() ? meal.name : "Meal " + (mealIndex + 1));
            binding.textViewMealTime.setText(meal.time != null && !meal.time.isEmpty() ? meal.time : "Not Set");
            
            // Note: Nutrition values are auto-calculated by the API, so we don't display them in edit mode

            // Setup food adapter
            foodAdapter = new NutritionFoodEditAdapter(dayIndex, mealIndex, new NutritionFoodEditAdapter.OnFoodActionListener() {
                @Override
                public void onEditFood(int dayIndex, int mealIndex, int foodIndex) {
                    listener.onEditFood(dayIndex, mealIndex, foodIndex);
                }

                @Override
                public void onRemoveFood(int dayIndex, int mealIndex, int foodIndex) {
                    listener.onRemoveFood(dayIndex, mealIndex, foodIndex);
                }
            }, viewModel);
            binding.recyclerViewEditFoods.setAdapter(foodAdapter);
            foodAdapter.updateFoods(meal.foods);

            // Set button listeners
            binding.buttonEditMeal.setOnClickListener(v -> listener.onEditMeal(dayIndex, mealIndex));
            binding.buttonAddFood.setOnClickListener(v -> listener.onAddFood(dayIndex, mealIndex));
            binding.buttonDeleteMeal.setOnClickListener(v -> listener.onRemoveMeal(dayIndex, mealIndex));
        }
    }
}
