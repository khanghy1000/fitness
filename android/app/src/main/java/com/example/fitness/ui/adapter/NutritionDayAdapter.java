package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.NutritionPlanDay;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class NutritionDayAdapter extends RecyclerView.Adapter<NutritionDayAdapter.NutritionDayViewHolder> {
    private List<NutritionPlanDay> days = new ArrayList<>();
    private OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(NutritionPlanDay day);
    }

    public NutritionDayAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NutritionDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nutrition_day, parent, false);
        return new NutritionDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NutritionDayViewHolder holder, int position) {
        NutritionPlanDay day = days.get(position);
        holder.bind(day, listener);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public void updateDays(List<NutritionPlanDay> days) {
        this.days.clear();
        this.days.addAll(days);
        notifyDataSetChanged();
    }

    public static class NutritionDayViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewDayName;
        private TextView textViewTotalCalories;
        private TextView textViewMacros;
        private RecyclerView recyclerViewMeals;
        private NutritionMealAdapter mealAdapter;

        public NutritionDayViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDayName = itemView.findViewById(R.id.textViewDayName);
            textViewTotalCalories = itemView.findViewById(R.id.textViewTotalCalories);
            textViewMacros = itemView.findViewById(R.id.textViewMacros);
            recyclerViewMeals = itemView.findViewById(R.id.recyclerViewMeals);
            
            recyclerViewMeals.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            mealAdapter = new NutritionMealAdapter(null);
            recyclerViewMeals.setAdapter(mealAdapter);
        }

        public void bind(NutritionPlanDay day, OnDayClickListener listener) {
            // Capitalize first letter of weekday
            String dayName = day.getWeekday().getValue();
            dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
            textViewDayName.setText(dayName);

            // Set total calories
            if (day.getTotalCalories() != null) {
                textViewTotalCalories.setText(day.getTotalCalories() + " cal");
            } else {
                textViewTotalCalories.setText("0 cal");
            }

            // Set macros
            StringBuilder macrosBuilder = new StringBuilder();
            if (day.getProtein() != null) {
                macrosBuilder.append("P: ").append(formatDecimal(day.getProtein())).append("g");
            }
            if (day.getCarbs() != null) {
                if (macrosBuilder.length() > 0) macrosBuilder.append(" | ");
                macrosBuilder.append("C: ").append(formatDecimal(day.getCarbs())).append("g");
            }
            if (day.getFat() != null) {
                if (macrosBuilder.length() > 0) macrosBuilder.append(" | ");
                macrosBuilder.append("F: ").append(formatDecimal(day.getFat())).append("g");
            }
            textViewMacros.setText(macrosBuilder.toString());

            // TODO: Load meals for this day and update meal adapter
            // This would require additional API calls to get meals for each day

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDayClick(day);
                }
            });
        }

        private String formatDecimal(BigDecimal decimal) {
            if (decimal == null) return "0";
            return decimal.stripTrailingZeros().toPlainString();
        }
    }
}
