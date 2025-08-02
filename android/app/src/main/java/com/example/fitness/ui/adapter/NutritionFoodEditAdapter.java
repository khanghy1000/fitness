package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.databinding.ItemNutritionFoodEditBinding;
import com.example.fitness.ui.viewmodel.NutritionPlanEditViewModel;

import java.util.ArrayList;
import java.util.List;

public class NutritionFoodEditAdapter extends RecyclerView.Adapter<NutritionFoodEditAdapter.FoodEditViewHolder> {
    private List<NutritionPlanEditViewModel.EditablePlanFood> foods = new ArrayList<>();
    private final int dayIndex;
    private final int mealIndex;
    private final OnFoodActionListener foodActionListener;

    public interface OnFoodActionListener {
        void onRemoveFood(int dayIndex, int mealIndex, int foodIndex);
    }

    public NutritionFoodEditAdapter(int dayIndex, int mealIndex, OnFoodActionListener foodActionListener) {
        this.dayIndex = dayIndex;
        this.mealIndex = mealIndex;
        this.foodActionListener = foodActionListener;
    }

    @NonNull
    @Override
    public FoodEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNutritionFoodEditBinding binding = ItemNutritionFoodEditBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FoodEditViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodEditViewHolder holder, int position) {
        NutritionPlanEditViewModel.EditablePlanFood food = foods.get(position);
        holder.bind(food, dayIndex, mealIndex, position, foodActionListener);
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    public void updateFoods(List<NutritionPlanEditViewModel.EditablePlanFood> foods) {
        this.foods.clear();
        this.foods.addAll(foods);
        notifyDataSetChanged();
    }

    public static class FoodEditViewHolder extends RecyclerView.ViewHolder {
        private final ItemNutritionFoodEditBinding binding;

        public FoodEditViewHolder(ItemNutritionFoodEditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(NutritionPlanEditViewModel.EditablePlanFood food, int dayIndex, int mealIndex, int foodIndex, OnFoodActionListener listener) {
            // Set food data
            binding.editTextFoodName.setText(food.name);
            binding.editTextFoodQuantity.setText(food.quantity);
            binding.editTextFoodCalories.setText(food.calories);

            // Set button listener
            binding.buttonDeleteFood.setOnClickListener(v -> listener.onRemoveFood(dayIndex, mealIndex, foodIndex));

            // Setup text change listeners to update the model
            setupTextChangeListeners(food);
        }

        private void setupTextChangeListeners(NutritionPlanEditViewModel.EditablePlanFood food) {
            // Add text watchers to update the food object when text changes
            // This is a simplified version - in a production app you'd want to use proper text watchers
        }
    }
}
