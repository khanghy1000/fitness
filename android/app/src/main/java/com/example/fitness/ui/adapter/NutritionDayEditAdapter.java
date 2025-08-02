package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.databinding.ItemNutritionDayEditBinding;
import com.example.fitness.ui.viewmodel.NutritionPlanEditViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NutritionDayEditAdapter extends RecyclerView.Adapter<NutritionDayEditAdapter.DayEditViewHolder> {
    private List<NutritionPlanEditViewModel.EditablePlanDay> days = new ArrayList<>();
    private final OnDayActionListener dayActionListener;

    public interface OnDayActionListener {
        void onAddMeal(int dayIndex);
        void onRemoveMeal(int dayIndex, int mealIndex);
        void onAddFood(int dayIndex, int mealIndex);
        void onRemoveFood(int dayIndex, int mealIndex, int foodIndex);
        void onRemoveDay(int dayIndex);
    }

    public NutritionDayEditAdapter(OnDayActionListener dayActionListener) {
        this.dayActionListener = dayActionListener;
    }

    @NonNull
    @Override
    public DayEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNutritionDayEditBinding binding = ItemNutritionDayEditBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DayEditViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DayEditViewHolder holder, int position) {
        NutritionPlanEditViewModel.EditablePlanDay day = days.get(position);
        holder.bind(day, position, dayActionListener);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public void updateDays(List<NutritionPlanEditViewModel.EditablePlanDay> days) {
        this.days.clear();
        this.days.addAll(days);
        notifyDataSetChanged();
    }

    public static class DayEditViewHolder extends RecyclerView.ViewHolder {
        private final ItemNutritionDayEditBinding binding;
        private NutritionMealEditAdapter mealAdapter;

        public DayEditViewHolder(ItemNutritionDayEditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            binding.recyclerViewEditMeals.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        }

        public void bind(NutritionPlanEditViewModel.EditablePlanDay day, int dayIndex, OnDayActionListener listener) {
            // Setup weekday dropdown
            String[] weekdays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            ArrayAdapter<String> weekdayAdapter = new ArrayAdapter<>(binding.getRoot().getContext(),
                    android.R.layout.simple_dropdown_item_1line, weekdays);
            binding.autoCompleteWeekday.setAdapter(weekdayAdapter);
            binding.autoCompleteWeekday.setText(capitalizeWeekday(day.weekday), false);

            // Set macro values
            binding.editTextCalories.setText(day.totalCalories);
            binding.editTextProtein.setText(day.protein);
            binding.editTextCarbs.setText(day.carbs);
            binding.editTextFat.setText(day.fat);

            // Setup meal adapter
            mealAdapter = new NutritionMealEditAdapter(dayIndex, new NutritionMealEditAdapter.OnMealActionListener() {
                @Override
                public void onAddFood(int dayIndex, int mealIndex) {
                    listener.onAddFood(dayIndex, mealIndex);
                }

                @Override
                public void onRemoveFood(int dayIndex, int mealIndex, int foodIndex) {
                    listener.onRemoveFood(dayIndex, mealIndex, foodIndex);
                }

                @Override
                public void onRemoveMeal(int dayIndex, int mealIndex) {
                    listener.onRemoveMeal(dayIndex, mealIndex);
                }
            });
            binding.recyclerViewEditMeals.setAdapter(mealAdapter);
            mealAdapter.updateMeals(day.meals);

            // Set button listeners
            binding.buttonAddMeal.setOnClickListener(v -> listener.onAddMeal(dayIndex));
            binding.buttonDeleteDay.setOnClickListener(v -> listener.onRemoveDay(dayIndex));

            // Setup text change listeners to update the model
            setupTextChangeListeners(day);
        }

        private void setupTextChangeListeners(NutritionPlanEditViewModel.EditablePlanDay day) {
            binding.autoCompleteWeekday.setOnItemClickListener((parent, view, position, id) -> {
                String[] weekdayValues = {"sun", "mon", "tue", "wed", "thu", "fri", "sat"};
                if (position >= 0 && position < weekdayValues.length) {
                    day.weekday = weekdayValues[position];
                }
            });
        }

        private String capitalizeWeekday(String weekday) {
            if (weekday == null || weekday.isEmpty()) return "";
            
            switch (weekday.toLowerCase()) {
                case "sun": return "Sunday";
                case "mon": return "Monday";
                case "tue": return "Tuesday";
                case "wed": return "Wednesday";
                case "thu": return "Thursday";
                case "fri": return "Friday";
                case "sat": return "Saturday";
                default: return weekday;
            }
        }
    }
}
