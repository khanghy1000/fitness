package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.databinding.ItemNutritionDayDisplayBinding;
import com.example.fitness.ui.viewmodel.NutritionPlanEditViewModel;

import java.util.ArrayList;
import java.util.List;

public class NutritionDayEditAdapter extends RecyclerView.Adapter<NutritionDayEditAdapter.DayEditViewHolder> {
    private List<NutritionPlanEditViewModel.EditablePlanDay> days = new ArrayList<>();
    private final OnDayActionListener dayActionListener;
    private final NutritionPlanEditViewModel viewModel;

    public interface OnDayActionListener {
        void onEditDay(int dayIndex);
        void onAddMeal(int dayIndex);
        void onEditMeal(int dayIndex, int mealIndex);
        void onRemoveMeal(int dayIndex, int mealIndex);
        void onAddFood(int dayIndex, int mealIndex);
        void onEditFood(int dayIndex, int mealIndex, int foodIndex);
        void onRemoveFood(int dayIndex, int mealIndex, int foodIndex);
        void onRemoveDay(int dayIndex);
    }

    public NutritionDayEditAdapter(OnDayActionListener dayActionListener, NutritionPlanEditViewModel viewModel) {
        this.dayActionListener = dayActionListener;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public DayEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNutritionDayDisplayBinding binding = ItemNutritionDayDisplayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DayEditViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DayEditViewHolder holder, int position) {
        NutritionPlanEditViewModel.EditablePlanDay day = days.get(position);
        holder.bind(day, position, dayActionListener, viewModel);
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
        private final ItemNutritionDayDisplayBinding binding;
        private NutritionMealEditAdapter mealAdapter;

        public DayEditViewHolder(ItemNutritionDayDisplayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            binding.recyclerViewEditMeals.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        }

        public void bind(NutritionPlanEditViewModel.EditablePlanDay day, int dayIndex, OnDayActionListener listener, NutritionPlanEditViewModel viewModel) {
            // Display weekday
            binding.textViewWeekday.setText(capitalizeWeekday(day.weekday));

            // Note: Nutrition values are auto-calculated by the API, so we don't display them in edit mode

            // Setup meal adapter
            mealAdapter = new NutritionMealEditAdapter(dayIndex, new NutritionMealEditAdapter.OnMealActionListener() {
                @Override
                public void onEditMeal(int dayIndex, int mealIndex) {
                    listener.onEditMeal(dayIndex, mealIndex);
                }

                @Override
                public void onAddFood(int dayIndex, int mealIndex) {
                    listener.onAddFood(dayIndex, mealIndex);
                }

                @Override
                public void onEditFood(int dayIndex, int mealIndex, int foodIndex) {
                    listener.onEditFood(dayIndex, mealIndex, foodIndex);
                }

                @Override
                public void onRemoveFood(int dayIndex, int mealIndex, int foodIndex) {
                    listener.onRemoveFood(dayIndex, mealIndex, foodIndex);
                }

                @Override
                public void onRemoveMeal(int dayIndex, int mealIndex) {
                    listener.onRemoveMeal(dayIndex, mealIndex);
                }
            }, viewModel);
            binding.recyclerViewEditMeals.setAdapter(mealAdapter);
            mealAdapter.updateMeals(day.meals);

            // Set button listeners
            binding.buttonEditDay.setOnClickListener(v -> listener.onEditDay(dayIndex));
            binding.buttonAddMeal.setOnClickListener(v -> listener.onAddMeal(dayIndex));
            binding.buttonDeleteDay.setOnClickListener(v -> listener.onRemoveDay(dayIndex));
        }

        private String capitalizeWeekday(String weekday) {
            if (weekday == null || weekday.isEmpty()) return "Not Set";
            
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
