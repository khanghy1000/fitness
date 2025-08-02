package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.databinding.ItemNutritionFoodDisplayBinding;
import com.example.fitness.ui.viewmodel.NutritionPlanEditViewModel;

import java.util.ArrayList;
import java.util.List;

public class NutritionFoodEditAdapter extends RecyclerView.Adapter<NutritionFoodEditAdapter.FoodEditViewHolder> {
    private List<NutritionPlanEditViewModel.EditablePlanFood> foods = new ArrayList<>();
    private final int dayIndex;
    private final int mealIndex;
    private final OnFoodActionListener foodActionListener;
    private final NutritionPlanEditViewModel viewModel;

    public interface OnFoodActionListener {
        void onEditFood(int dayIndex, int mealIndex, int foodIndex);
        void onRemoveFood(int dayIndex, int mealIndex, int foodIndex);
    }

    public NutritionFoodEditAdapter(int dayIndex, int mealIndex, OnFoodActionListener foodActionListener, NutritionPlanEditViewModel viewModel) {
        this.dayIndex = dayIndex;
        this.mealIndex = mealIndex;
        this.foodActionListener = foodActionListener;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public FoodEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNutritionFoodDisplayBinding binding = ItemNutritionFoodDisplayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FoodEditViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodEditViewHolder holder, int position) {
        NutritionPlanEditViewModel.EditablePlanFood food = foods.get(position);
        holder.bind(food, dayIndex, mealIndex, position, foodActionListener, viewModel);
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
        private final ItemNutritionFoodDisplayBinding binding;

        public FoodEditViewHolder(ItemNutritionFoodDisplayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(NutritionPlanEditViewModel.EditablePlanFood food, int dayIndex, int mealIndex, int foodIndex, OnFoodActionListener listener, NutritionPlanEditViewModel viewModel) {
            // Display food data
            binding.textViewFoodName.setText(food.name != null && !food.name.isEmpty() ? food.name : "Food " + (foodIndex + 1));
            binding.textViewFoodQuantity.setText(food.quantity != null && !food.quantity.isEmpty() ? food.quantity : "Not Set");
            binding.textViewFoodCalories.setText(food.calories != null && !food.calories.isEmpty() ? food.calories : "0");

            // Set button listeners
            binding.buttonEditFood.setOnClickListener(v -> listener.onEditFood(dayIndex, mealIndex, foodIndex));
            binding.buttonDeleteFood.setOnClickListener(v -> listener.onRemoveFood(dayIndex, mealIndex, foodIndex));
        }
    }
}
