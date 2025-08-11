package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.DetailedNutritionAdherenceHistory;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NutritionAdherenceAdapter extends RecyclerView.Adapter<NutritionAdherenceAdapter.AdherenceViewHolder> {
    private List<DetailedNutritionAdherenceHistory> adherenceHistory = new ArrayList<>();

    @NonNull
    @Override
    public AdherenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nutrition_adherence, parent, false);
        return new AdherenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdherenceViewHolder holder, int position) {
        DetailedNutritionAdherenceHistory history = adherenceHistory.get(position);
        holder.bind(history);
    }

    @Override
    public int getItemCount() {
        return adherenceHistory.size();
    }

    public void updateAdherence(List<DetailedNutritionAdherenceHistory> history) {
        this.adherenceHistory.clear();
        if (history != null) {
            this.adherenceHistory.addAll(history);
        }
        notifyDataSetChanged();
    }

    public static class AdherenceViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView textViewDate;
        private TextView textViewWeekday;
        private TextView textViewMealsCompleted;
        private TextView textViewCaloriesConsumed;
        private TextView textViewNotes;
        private ProgressBar progressBarAdherence;
        private TextView textViewAdherencePercentage;

        public AdherenceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewWeekday = itemView.findViewById(R.id.textViewWeekday);
            textViewMealsCompleted = itemView.findViewById(R.id.textViewMealsCompleted);
            textViewCaloriesConsumed = itemView.findViewById(R.id.textViewCaloriesConsumed);
            textViewNotes = itemView.findViewById(R.id.textViewNotes);
            progressBarAdherence = itemView.findViewById(R.id.progressBarAdherence);
            textViewAdherencePercentage = itemView.findViewById(R.id.textViewAdherencePercentage);
        }

        public void bind(DetailedNutritionAdherenceHistory history) {
            // Format and display date
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(history.getDate());
                textViewDate.setText(outputFormat.format(date));
            } catch (Exception e) {
                textViewDate.setText(history.getDate());
            }
            
            // Display weekday
            textViewWeekday.setText(getDayDisplayName(history.getWeekday()));
            
            // Display meals completed
            String mealsText = String.format(Locale.getDefault(), "%d/%d meals completed",
                    history.getMealsCompleted() != null ? history.getMealsCompleted() : 0,
                    history.getTotalMeals() != null ? history.getTotalMeals() : 0);
            textViewMealsCompleted.setText(mealsText);
            
            // Display calories
            String caloriesText = String.format(Locale.getDefault(), "%d/%d calories",
                    history.getTotalCaloriesConsumed() != null ? history.getTotalCaloriesConsumed() : 0,
                    history.getTotalCaloriesPlanned() != null ? history.getTotalCaloriesPlanned() : 0);
            textViewCaloriesConsumed.setText(caloriesText);
            
            // Display adherence percentage
            if (history.getAdherencePercentage() != null) {
                int adherencePercent = history.getAdherencePercentage().intValue();
                progressBarAdherence.setProgress(adherencePercent);
                textViewAdherencePercentage.setText(adherencePercent + "%");
                
                // Color coding for adherence
                if (adherencePercent >= 80) {
                    progressBarAdherence.setProgressTintList(
                        itemView.getContext().getColorStateList(android.R.color.holo_green_dark));
                } else if (adherencePercent >= 60) {
                    progressBarAdherence.setProgressTintList(
                        itemView.getContext().getColorStateList(android.R.color.holo_orange_dark));
                } else {
                    progressBarAdherence.setProgressTintList(
                        itemView.getContext().getColorStateList(android.R.color.holo_red_dark));
                }
            } else {
                progressBarAdherence.setProgress(0);
                textViewAdherencePercentage.setText("-%");
            }
            
            // Display notes
            if (history.getNotes() != null && !history.getNotes().trim().isEmpty()) {
                textViewNotes.setText(history.getNotes());
                textViewNotes.setVisibility(View.VISIBLE);
            } else {
                textViewNotes.setVisibility(View.GONE);
            }
        }

        private String getDayDisplayName(DetailedNutritionAdherenceHistory.Weekday weekday) {
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
