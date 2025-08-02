package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.NutritionPlanFood;

import java.util.ArrayList;
import java.util.List;

public class NutritionFoodAdapter extends RecyclerView.Adapter<NutritionFoodAdapter.NutritionFoodViewHolder> {
    private List<NutritionPlanFood> foods = new ArrayList<>();
    private OnFoodClickListener listener;

    public interface OnFoodClickListener {
        void onFoodClick(NutritionPlanFood food);
    }

    public NutritionFoodAdapter(OnFoodClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NutritionFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nutrition_food, parent, false);
        return new NutritionFoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NutritionFoodViewHolder holder, int position) {
        NutritionPlanFood food = foods.get(position);
        holder.bind(food, listener);
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    public void updateFoods(List<NutritionPlanFood> foods) {
        this.foods.clear();
        this.foods.addAll(foods);
        notifyDataSetChanged();
    }

    public static class NutritionFoodViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewFoodName;
        private TextView textViewFoodQuantity;
        private TextView textViewFoodCalories;

        public NutritionFoodViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewFoodName = itemView.findViewById(R.id.textViewFoodName);
            textViewFoodQuantity = itemView.findViewById(R.id.textViewFoodQuantity);
            textViewFoodCalories = itemView.findViewById(R.id.textViewFoodCalories);
        }

        public void bind(NutritionPlanFood food, OnFoodClickListener listener) {
            textViewFoodName.setText(food.getName());
            textViewFoodQuantity.setText(food.getQuantity());
            textViewFoodCalories.setText(food.getCalories() + " cal");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFoodClick(food);
                }
            });
        }
    }
}
