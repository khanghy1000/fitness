package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.NutritionPlanMeal;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NutritionMealAdapter extends RecyclerView.Adapter<NutritionMealAdapter.NutritionMealViewHolder> {
    private List<NutritionPlanMeal> meals = new ArrayList<>();
    private OnMealClickListener listener;

    public interface OnMealClickListener {
        void onMealClick(NutritionPlanMeal meal);
    }

    public NutritionMealAdapter(OnMealClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NutritionMealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nutrition_meal, parent, false);
        return new NutritionMealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NutritionMealViewHolder holder, int position) {
        NutritionPlanMeal meal = meals.get(position);
        holder.bind(meal, listener);
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public void updateMeals(List<NutritionPlanMeal> meals) {
        this.meals.clear();
        this.meals.addAll(meals);
        notifyDataSetChanged();
    }

    public static class NutritionMealViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMealName;
        private TextView textViewMealTime;
        private TextView textViewMealCalories;
        private RecyclerView recyclerViewFoods;
        private NutritionFoodAdapter foodAdapter;

        public NutritionMealViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMealName = itemView.findViewById(R.id.textViewMealName);
            textViewMealTime = itemView.findViewById(R.id.textViewMealTime);
            textViewMealCalories = itemView.findViewById(R.id.textViewMealCalories);
            recyclerViewFoods = itemView.findViewById(R.id.recyclerViewFoods);
            
            recyclerViewFoods.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            foodAdapter = new NutritionFoodAdapter(null);
            recyclerViewFoods.setAdapter(foodAdapter);
        }

        public void bind(NutritionPlanMeal meal, OnMealClickListener listener) {
            textViewMealName.setText(meal.getName());

            // Format time
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date time = inputFormat.parse(meal.getTime());
                textViewMealTime.setText(outputFormat.format(time));
            } catch (Exception e) {
                textViewMealTime.setText(meal.getTime());
            }

            // Set calories
            if (meal.getCalories() != null) {
                textViewMealCalories.setText(meal.getCalories() + " cal");
            } else {
                textViewMealCalories.setText("0 cal");
            }

            // TODO: Load foods for this meal and update food adapter
            // This would require additional API calls to get foods for each meal

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMealClick(meal);
                }
            });
        }
    }
}
