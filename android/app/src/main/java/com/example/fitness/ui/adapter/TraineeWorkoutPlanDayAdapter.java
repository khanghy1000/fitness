package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.DetailedWorkoutPlanDay;
import com.example.fitness.data.network.model.generated.WorkoutPlanResults;
import com.example.fitness.data.network.model.generated.WorkoutPlanResultsWorkoutPlanDaysInner;
import com.example.fitness.data.network.model.generated.WorkoutPlanResultsWorkoutPlanDaysInnerExercisesInner;
import com.example.fitness.databinding.ItemTraineeWorkoutPlanDayBinding;
import com.example.fitness.utils.DateUtils;
import com.example.fitness.utils.DurationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TraineeWorkoutPlanDayAdapter extends RecyclerView.Adapter<TraineeWorkoutPlanDayAdapter.DayViewHolder> {
    private List<DetailedWorkoutPlanDay> days = new ArrayList<>();
    private WorkoutPlanResults workoutPlanResults;
    private OnDayClickListener listener;
    private String startDate; // Start date of the workout plan assignment
    private boolean isCompleted = false; // Whether this is a completed workout plan
    private boolean isCoachMode = false; // Whether this is being viewed by a coach

    public interface OnDayClickListener {
        void onDayClick(DetailedWorkoutPlanDay day);
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setWorkoutPlanResults(WorkoutPlanResults results) {
        this.workoutPlanResults = results;
        notifyDataSetChanged();
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
        notifyDataSetChanged();
    }
    
    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
        notifyDataSetChanged();
    }
    
    public void setIsCoachMode(boolean isCoachMode) {
        this.isCoachMode = isCoachMode;
        notifyDataSetChanged();
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
            
            // Show/hide Today chip and apply styling - but not for completed plans
            if (!isCompleted && startDate != null && DateUtils.isCurrentDay(startDate, day.getDay())) {
                // Current day styling
                binding.chipToday.setVisibility(android.view.View.VISIBLE);
                binding.textViewDayNumber.setTextColor(binding.getRoot().getContext().getResources().getColor(com.example.fitness.R.color.current_day_text));
                binding.cardDay.setStrokeColor(binding.getRoot().getContext().getResources().getColor(com.example.fitness.R.color.current_day_border));
                binding.cardDay.setStrokeWidth(3);
                binding.cardDay.setCardBackgroundColor(binding.getRoot().getContext().getResources().getColor(com.example.fitness.R.color.current_day_background));
            } else {
                // Normal day styling
                binding.chipToday.setVisibility(android.view.View.GONE);
                binding.textViewDayNumber.setTextColor(binding.getRoot().getContext().getResources().getColor(android.R.color.black));
                binding.cardDay.setStrokeWidth(0);
                binding.cardDay.setCardBackgroundColor(binding.getRoot().getContext().getResources().getColor(R.color.surface));
            }
            
            if (day.isRestDay()) {
                // Rest day
                binding.textViewDuration.setText("Rest Day");
                binding.textViewCalories.setText("-");
                binding.textViewExercises.setText("Rest");
                binding.cardDay.setAlpha(0.6f);
                binding.layoutProgress.setVisibility(View.GONE);
            } else {
                // Workout day
                // Duration (convert from seconds to formatted string)
                int duration = day.getDuration() != null ? day.getDuration() : 0;
                binding.textViewDuration.setText(DurationUtil.formatDuration(duration));
                
                // Calories
                binding.textViewCalories.setText("~" + (day.getEstimatedCalories() != null ? day.getEstimatedCalories() : 0) + " cal");
                
                // Exercise count
                int exerciseCount = day.getExercises() != null ? day.getExercises().size() : 0;
                binding.textViewExercises.setText(exerciseCount + " exercises");
                
                binding.cardDay.setAlpha(1.0f);
                
                // Show progress if we have workout plan results
                if (workoutPlanResults != null && workoutPlanResults.getWorkoutPlanDays() != null) {
                    showDayProgress(day);
                } else {
                    binding.layoutProgress.setVisibility(View.GONE);
                }
            }
            
            // Click listener
            binding.cardDay.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDayClick(day);
                }
            });
        }

        private void showDayProgress(DetailedWorkoutPlanDay day) {
            // Find this day in the results
            WorkoutPlanResultsWorkoutPlanDaysInner dayResults = null;
            for (WorkoutPlanResultsWorkoutPlanDaysInner resultDay : workoutPlanResults.getWorkoutPlanDays()) {
                if (resultDay.getDay() == day.getDay()) {
                    dayResults = resultDay;
                    break;
                }
            }

            if (dayResults == null || dayResults.getExercises() == null) {
                binding.layoutProgress.setVisibility(View.GONE);
                return;
            }

            // Count completed exercises
            int totalExercises = day.getExercises() != null ? day.getExercises().size() : 0;
            int completedExercises = 0;

            for (WorkoutPlanResultsWorkoutPlanDaysInnerExercisesInner exerciseResult : dayResults.getExercises()) {
                if (exerciseResult.getExerciseResults() != null && !exerciseResult.getExerciseResults().isEmpty()) {
                    completedExercises++;
                }
            }

            if (totalExercises > 0) {
                binding.layoutProgress.setVisibility(View.VISIBLE);
                binding.textViewProgress.setText(completedExercises + "/" + totalExercises + " completed");
                
                int progressPercentage = (completedExercises * 100) / totalExercises;
                binding.progressBarDay.setProgress(progressPercentage);
            } else {
                binding.layoutProgress.setVisibility(View.GONE);
            }
        }
    }
}
