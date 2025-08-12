package com.example.fitness.ui.activity.trainee;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.DetailedNutritionAdherenceHistory;
import com.example.fitness.databinding.ActivityTraineeNutritionProgressBinding;
import com.example.fitness.ui.adapter.NutritionAdherenceAdapter;
import com.example.fitness.ui.viewmodel.TraineeNutritionPlanViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TraineeNutritionProgressActivity extends AppCompatActivity {

    private ActivityTraineeNutritionProgressBinding binding;
    private TraineeNutritionPlanViewModel viewModel;
    private NutritionAdherenceAdapter adherenceAdapter;
    
    private int assignmentId;
    private int planId;
    private String planName;
    private String traineeId;
    private String traineeName;
    private String startDate;
    private String endDate;
    private List<DetailedNutritionAdherenceHistory> currentAdherenceHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityTraineeNutritionProgressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getIntentData();
        initializeViews();
        setupViewModel();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
        loadData();
    }

    private void getIntentData() {
        assignmentId = getIntent().getIntExtra("ASSIGNMENT_ID", -1);
        planId = getIntent().getIntExtra("PLAN_ID", -1);
        planName = getIntent().getStringExtra("PLAN_NAME");
        traineeId = getIntent().getStringExtra("TRAINEE_ID");
        traineeName = getIntent().getStringExtra("TRAINEE_NAME");
        startDate = getIntent().getStringExtra("START_DATE");
        endDate = getIntent().getStringExtra("END_DATE");
    }

    private void initializeViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nutrition Progress");
        }
        
        binding.textViewPlanName.setText(planName != null ? planName : "Nutrition Plan");
        
        // Set trainee name
        if (traineeName != null && !traineeName.trim().isEmpty()) {
            binding.textViewTraineeName.setText("Trainee: " + traineeName);
        } else {
            binding.textViewTraineeName.setText("Trainee: Unknown");
        }
        
        // Set plan dates
        displayPlanDates();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TraineeNutritionPlanViewModel.class);
    }

    private void setupRecyclerView() {
        adherenceAdapter = new NutritionAdherenceAdapter();
        binding.recyclerViewAdherence.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewAdherence.setAdapter(adherenceAdapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadData);
    }

    private void displayPlanDates() {
        StringBuilder datesText = new StringBuilder();
        
        // Format start date
        if (startDate != null && !startDate.trim().isEmpty()) {
            String formattedStartDate = formatDate(startDate);
            datesText.append("Started: ").append(formattedStartDate);
        } else {
            datesText.append("Start date: Not specified");
        }
        
        // Format end date (optional)
        if (endDate != null && !endDate.trim().isEmpty()) {
            String formattedEndDate = formatDate(endDate);
            datesText.append(" • Ends: ").append(formattedEndDate);
        }
        
        binding.textViewPlanDates.setText(datesText.toString());
    }

    private String formatDate(String dateString) {
        try {
            // Parse the date (assuming it's in yyyy-MM-dd format from API)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            // If parsing fails, return the original string
            android.util.Log.w("DateFormat", "Failed to parse date: " + dateString, e);
            return dateString;
        }
    }

    private void observeViewModel() {
        viewModel.nutritionAdherenceHistory.observe(this, adherenceHistory -> {
            android.util.Log.d("NutritionProgress", "Adherence history received: " + (adherenceHistory != null ? adherenceHistory.size() : "null") + " items");
            currentAdherenceHistory = adherenceHistory != null ? adherenceHistory : new ArrayList<>();
            
            displayProgressData();
        });

        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (!isLoading) {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        viewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
    }

    private void loadData() {
        android.util.Log.d("NutritionProgress", "Loading adherence data - planId: " + planId + ", assignmentId: " + assignmentId + ", traineeId: " + traineeId);
        if (assignmentId != -1 && traineeId != null) {
            viewModel.loadNutritionAdherenceHistory(String.valueOf(assignmentId), traineeId);
        }
    }

    private void displayProgressData() {
        if (currentAdherenceHistory.isEmpty()) {
            binding.textViewEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewAdherence.setVisibility(View.GONE);
            
            // Initialize with empty stats
            binding.textViewWeeklyAdherence.setText("Weekly Adherence: N/A");
            binding.textViewMonthlyAverage.setText("Monthly Average: N/A");
            binding.textViewTotalMealsCompleted.setText("Meals Completed: 0/0");
        } else {
            binding.textViewEmptyState.setVisibility(View.GONE);
            binding.recyclerViewAdherence.setVisibility(View.VISIBLE);
            adherenceAdapter.updateAdherence(currentAdherenceHistory);
            updateProgressStats(currentAdherenceHistory);
        }
    }
    
    private void updateProgressStats(List<DetailedNutritionAdherenceHistory> adherenceHistory) {
        if (adherenceHistory == null || adherenceHistory.isEmpty()) {
            binding.textViewWeeklyAdherence.setText("Weekly Adherence: No data available");
            binding.textViewMonthlyAverage.setText("Monthly Average: No data available");
            binding.textViewTotalMealsCompleted.setText("Start completing meals to see your progress!");
            return;
        }
        
        // Calculate weekly adherence (last 7 days)
        int recentDays = Math.min(7, adherenceHistory.size());
        double weeklyAdherence = 0;
        int totalMealsCompleted = 0;
        int totalMealsPlanned = 0;
        
        for (int i = 0; i < recentDays; i++) {
            DetailedNutritionAdherenceHistory day = adherenceHistory.get(i);
            if (day.getAdherencePercentage() != null) {
                weeklyAdherence += day.getAdherencePercentage().doubleValue();
            }
            if (day.getMealsCompleted() != null) {
                totalMealsCompleted += day.getMealsCompleted();
            }
            if (day.getTotalMeals() != null) {
                totalMealsPlanned += day.getTotalMeals();
            }
        }
        
        if (recentDays > 0) {
            weeklyAdherence = weeklyAdherence / recentDays;
        }
        
        // Calculate monthly average
        double monthlyAverage = 0;
        int monthlyDays = Math.min(30, adherenceHistory.size());
        for (int i = 0; i < monthlyDays; i++) {
            DetailedNutritionAdherenceHistory day = adherenceHistory.get(i);
            if (day.getAdherencePercentage() != null) {
                monthlyAverage += day.getAdherencePercentage().doubleValue();
            }
        }
        if (monthlyDays > 0) {
            monthlyAverage = monthlyAverage / monthlyDays;
        }
        
        binding.textViewWeeklyAdherence.setText(String.format(Locale.getDefault(), 
            "Weekly Adherence: %.1f%%", weeklyAdherence));
        binding.textViewMonthlyAverage.setText(String.format(Locale.getDefault(), 
            "Monthly Average: %.1f%%", monthlyAverage));
        binding.textViewTotalMealsCompleted.setText(String.format(Locale.getDefault(), 
            "Meals Completed: %d/%d", totalMealsCompleted, totalMealsPlanned));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
