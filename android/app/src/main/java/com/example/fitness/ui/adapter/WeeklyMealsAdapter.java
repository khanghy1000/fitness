package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.DetailedNutritionPlan;
import com.example.fitness.data.network.model.generated.DetailedNutritionPlanDay;
import com.example.fitness.data.network.model.generated.DetailedNutritionPlanMeal;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class WeeklyMealsAdapter extends RecyclerView.Adapter<WeeklyMealsAdapter.DayViewHolder> {
    private List<DetailedNutritionPlanDay> days = new ArrayList<>();

    public WeeklyMealsAdapter() {
        // No meal action listener needed for weekly view
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weekly_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DetailedNutritionPlanDay day = days.get(position);
        holder.bind(day);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public void updatePlan(DetailedNutritionPlan plan) {
        this.days.clear();
        if (plan != null && plan.getDays() != null) {
            this.days.addAll(plan.getDays());
        }
        notifyDataSetChanged();
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView textViewDayName;
        private TextView textViewDayMacros;
        private RecyclerView recyclerViewDayMeals;
        private TextView textViewEmptyMeals;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            textViewDayName = itemView.findViewById(R.id.textViewDayName);
            textViewDayMacros = itemView.findViewById(R.id.textViewDayMacros);
            recyclerViewDayMeals = itemView.findViewById(R.id.recyclerViewDayMeals);
            textViewEmptyMeals = itemView.findViewById(R.id.textViewEmptyMeals);
        }

        public void bind(DetailedNutritionPlanDay day) {
            // Set day name
            textViewDayName.setText(getDayDisplayName(day.getWeekday()));
            
            // Set day macros
            StringBuilder macros = new StringBuilder();
            if (day.getTotalCalories() != null) {
                macros.append(day.getTotalCalories()).append(" cal");
            } else {
                macros.append("- cal");
            }
            
            if (day.getProtein() != null) {
                macros.append(" | P: ").append(Math.round(day.getProtein().doubleValue())).append("g");
            }
            if (day.getCarbs() != null) {
                macros.append(" | C: ").append(Math.round(day.getCarbs().doubleValue())).append("g");
            }
            if (day.getFat() != null) {
                macros.append(" | F: ").append(Math.round(day.getFat().doubleValue())).append("g");
            }
            
            textViewDayMacros.setText(macros.toString());
            
            // Set up meals RecyclerView with read-only adapter
            if (day.getMeals() != null && !day.getMeals().isEmpty()) {
                ReadOnlyMealsAdapter mealsAdapter = new ReadOnlyMealsAdapter();
                mealsAdapter.updateMeals(day.getMeals());
                
                recyclerViewDayMeals.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                recyclerViewDayMeals.setAdapter(mealsAdapter);
                recyclerViewDayMeals.setVisibility(View.VISIBLE);
                textViewEmptyMeals.setVisibility(View.GONE);
            } else {
                recyclerViewDayMeals.setVisibility(View.GONE);
                textViewEmptyMeals.setVisibility(View.VISIBLE);
            }
        }

        private String getDayDisplayName(DetailedNutritionPlanDay.Weekday weekday) {
            switch (weekday) {
                case sun: return "Sunday";
                case mon: return "Monday";
                case tue: return "Tuesday";
                case wed: return "Wednesday";
                case thu: return "Thursday";
                case fri: return "Friday";
                case sat: return "Saturday";
                default: return weekday.name();
            }
        }
    }
}
