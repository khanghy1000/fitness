package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.DetailedWorkoutPlanDay;
import com.example.fitness.data.repository.ExercisesRepository;

import java.util.ArrayList;
import java.util.List;

public class WorkoutDayDetailAdapter extends RecyclerView.Adapter<WorkoutDayDetailAdapter.WorkoutDayViewHolder> {
    private List<DetailedWorkoutPlanDay> days = new ArrayList<>();
    private ExercisesRepository exercisesRepository;

    public void setExercisesRepository(ExercisesRepository exercisesRepository) {
        this.exercisesRepository = exercisesRepository;
    }

    @NonNull
    @Override
    public WorkoutDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_day_detail, parent, false);
        return new WorkoutDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutDayViewHolder holder, int position) {
        DetailedWorkoutPlanDay day = days.get(position);
        holder.bind(day, exercisesRepository);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public void updateDays(List<DetailedWorkoutPlanDay> days) {
        this.days.clear();
        this.days.addAll(days);
        notifyDataSetChanged();
    }

    public static class WorkoutDayViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewDayNumber;
        private TextView textViewRestDay;
        private TextView textViewDayCalories;
        private RecyclerView recyclerViewExercises;
        private WorkoutExerciseDetailAdapter exerciseAdapter;

        public WorkoutDayViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDayNumber = itemView.findViewById(R.id.textViewDayNumber);
            textViewRestDay = itemView.findViewById(R.id.textViewRestDay);
            textViewDayCalories = itemView.findViewById(R.id.textViewDayCalories);
            recyclerViewExercises = itemView.findViewById(R.id.recyclerViewExercises);

            exerciseAdapter = new WorkoutExerciseDetailAdapter();
            recyclerViewExercises.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            recyclerViewExercises.setAdapter(exerciseAdapter);
        }

        public void bind(DetailedWorkoutPlanDay day, ExercisesRepository exercisesRepository) {
            textViewDayNumber.setText("Day " + day.getDay());

            // Set repository for exercise adapter
            if (exerciseAdapter != null && exercisesRepository != null) {
                exerciseAdapter.setExercisesRepository(exercisesRepository);
            }

            if (day.isRestDay()) {
                textViewRestDay.setVisibility(View.VISIBLE);
                recyclerViewExercises.setVisibility(View.GONE);
                textViewDayCalories.setVisibility(View.GONE);
            } else {
                textViewRestDay.setVisibility(View.GONE);
                recyclerViewExercises.setVisibility(View.VISIBLE);
                
                // Show calories if available
                if (day.getEstimatedCalories() != null) {
                    textViewDayCalories.setText("~" + day.getEstimatedCalories() + " cal");
                    textViewDayCalories.setVisibility(View.VISIBLE);
                } else {
                    textViewDayCalories.setVisibility(View.GONE);
                }

                exerciseAdapter.updateExercises(day.getExercises());
            }
        }
    }
}
