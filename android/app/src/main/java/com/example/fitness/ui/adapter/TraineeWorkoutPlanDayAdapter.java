package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.data.network.model.generated.DetailedWorkoutPlanDay;
import com.example.fitness.databinding.ItemTraineeWorkoutPlanDayBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TraineeWorkoutPlanDayAdapter extends RecyclerView.Adapter<TraineeWorkoutPlanDayAdapter.DayViewHolder> {
    private List<DetailedWorkoutPlanDay> days = new ArrayList<>();
    private OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(DetailedWorkoutPlanDay day);
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTraineeWorkoutPlanDayBinding binding = ItemTraineeWorkoutPlanDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DetailedWorkoutPlanDay day = days.get(position);
        holder.bind(day);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public void updateDays(List<DetailedWorkoutPlanDay> newDays) {
        this.days.clear();
        if (newDays != null) {
            this.days.addAll(newDays);
            // Sort by day number
            Collections.sort(this.days, new Comparator<DetailedWorkoutPlanDay>() {
                @Override
                public int compare(DetailedWorkoutPlanDay o1, DetailedWorkoutPlanDay o2) {
                    return Integer.compare(o1.getDay(), o2.getDay());
                }
            });
        }
        notifyDataSetChanged();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private final ItemTraineeWorkoutPlanDayBinding binding;

        public DayViewHolder(ItemTraineeWorkoutPlanDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DetailedWorkoutPlanDay day) {
            // Day number
            binding.textViewDayNumber.setText("Day " + day.getDay());
            
            if (day.isRestDay()) {
                // Rest day
                binding.textViewDuration.setText("Rest Day");
                binding.textViewCalories.setText("-");
                binding.textViewExercises.setText("Rest");
                binding.cardDay.setAlpha(0.6f);
            } else {
                // Workout day
                // Duration in minutes (convert from seconds)
                int durationInMinutes = day.getDuration() != null ? day.getDuration() / 60 : 0;
                binding.textViewDuration.setText(durationInMinutes + " min");
                
                // Calories
                binding.textViewCalories.setText("~" + (day.getEstimatedCalories() != null ? day.getEstimatedCalories() : 0) + " cal");
                
                // Exercise count
                int exerciseCount = day.getExercises() != null ? day.getExercises().size() : 0;
                binding.textViewExercises.setText(exerciseCount + " exercises");
                
                binding.cardDay.setAlpha(1.0f);
            }
            
            // Click listener
            binding.cardDay.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDayClick(day);
                }
            });
        }
    }
}
