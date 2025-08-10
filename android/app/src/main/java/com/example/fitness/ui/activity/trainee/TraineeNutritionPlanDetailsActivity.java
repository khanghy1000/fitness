package com.example.fitness.ui.activity.trainee;

import android.content.Intent;
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
import com.example.fitness.data.network.model.generated.DetailedNutritionPlan;
import com.example.fitness.data.network.model.generated.DetailedNutritionPlanDay;
import com.example.fitness.data.network.model.generated.DetailedNutritionPlanMeal;
import com.example.fitness.data.network.model.generated.MealCompletion;
import com.example.fitness.data.network.model.generated.NutritionAdherenceHistory;
import com.example.fitness.databinding.ActivityTraineeNutritionPlanDetailsBinding;
import com.example.fitness.ui.activity.NutritionPlanEditActivity;
import com.example.fitness.ui.adapter.TodayMealsAdapter;
import com.example.fitness.ui.adapter.WeeklyMealsAdapter;
import com.example.fitness.ui.adapter.NutritionAdherenceAdapter;
import com.example.fitness.ui.viewmodel.TraineeNutritionPlanViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TraineeNutritionPlanDetailsActivity extends AppCompatActivity implements TodayMealsAdapter.OnMealActionListener {

    private ActivityTraineeNutritionPlanDetailsBinding binding;
    private TraineeNutritionPlanViewModel viewModel;
    private TodayMealsAdapter todayMealsAdapter;
    private WeeklyMealsAdapter weeklyMealsAdapter;
    private NutritionAdherenceAdapter adherenceAdapter;
    
    private int assignmentId;
    private int planId;
    private String planName;
    private DetailedNutritionPlan currentPlan;
    
    // View mode tracking
    private enum ViewMode {
        TODAY, WEEKLY, PROGRESS
    }
    private ViewMode currentViewMode = ViewMode.TODAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityTraineeNutritionPlanDetailsBinding.inflate(getLayoutInflater());
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
        loadPlanDetails();
    }

    private void getIntentData() {
        assignmentId = getIntent().getIntExtra("ASSIGNMENT_ID", -1);
        planId = getIntent().getIntExtra("PLAN_ID", -1);
        planName = getIntent().getStringExtra("PLAN_NAME");
    }

    private void initializeViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        binding.textViewPlanName.setText(planName != null ? planName : "Nutrition Plan");
        
        // Set initial view mode
        setViewMode(ViewMode.TODAY);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TraineeNutritionPlanViewModel.class);
    }

    private void setupRecyclerView() {
        todayMealsAdapter = new TodayMealsAdapter(this);
        binding.recyclerViewMeals.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewMeals.setAdapter(todayMealsAdapter);
        
        // Initialize other adapters
        weeklyMealsAdapter = new WeeklyMealsAdapter();
        adherenceAdapter = new NutritionAdherenceAdapter();
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.swipeRefreshLayout.setOnRefreshListener(() -> loadPlanDetails());
        
        binding.buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, NutritionPlanEditActivity.class);
            intent.putExtra("PLAN_ID", planId);
            intent.putExtra("PLAN_NAME", planName);
            startActivity(intent);
        });
        
        // Tab selection listener
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        setViewMode(ViewMode.TODAY);
                        break;
                    case 1:
                        setViewMode(ViewMode.WEEKLY);
                        break;
                    case 2:
                        setViewMode(ViewMode.PROGRESS);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
    }

    private void observeViewModel() {
        viewModel.detailedNutritionPlan.observe(this, plan -> {
            if (plan != null) {
                currentPlan = plan;
                refreshCurrentView();
                updateEditButtonVisibility(plan);
            }
        });

        viewModel.nutritionAdherenceHistory.observe(this, adherenceHistory -> {
            android.util.Log.d("AdherenceDebug", "Activity received adherence history: " + (adherenceHistory != null ? adherenceHistory.size() : "null") + " items");
            if (adherenceHistory != null) {
                adherenceAdapter.updateAdherence(adherenceHistory);
                updateProgressStats(adherenceHistory);
                
                // If we're currently in progress view, refresh the display
                if (currentViewMode == ViewMode.PROGRESS) {
                    displayProgressView();
                }
            }
        });

        viewModel.currentUserId.observe(this, currentUserId -> {
            if (currentUserId != null && currentPlan != null) {
                updateEditButtonVisibility(currentPlan);
            }
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

        viewModel.successMessage.observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                viewModel.clearSuccessMessage();
                // Refresh the plan after successful meal completion
                loadPlanDetails();
            }
        });
    }

    private void loadPlanDetails() {
        android.util.Log.d("AdherenceDebug", "Loading plan details - planId: " + planId + ", assignmentId: " + assignmentId);
        if (planId != -1) {
            viewModel.loadNutritionPlanDetails(String.valueOf(planId));
            // Also load adherence history for progress tracking
            if (assignmentId != -1) {
                android.util.Log.d("AdherenceDebug", "Loading adherence history for assignmentId: " + assignmentId);
                viewModel.loadNutritionAdherenceHistory(String.valueOf(assignmentId));
            } else {
                android.util.Log.w("AdherenceDebug", "Assignment ID is -1, cannot load adherence history");
            }
        }
    }

    private void setViewMode(ViewMode mode) {
        currentViewMode = mode;
        
        // Update tab selection
        TabLayout.Tab tabToSelect = null;
        switch (mode) {
            case TODAY:
                tabToSelect = binding.tabLayout.getTabAt(0);
                break;
            case WEEKLY:
                tabToSelect = binding.tabLayout.getTabAt(1);
                break;
            case PROGRESS:
                tabToSelect = binding.tabLayout.getTabAt(2);
                break;
        }
        
        if (tabToSelect != null && !tabToSelect.isSelected()) {
            tabToSelect.select();
        }
        
        // Update toolbar title
        switch (mode) {
            case TODAY:
                binding.toolbar.setTitle("Today's Meals");
                break;
            case WEEKLY:
                binding.toolbar.setTitle("Weekly Overview");
                break;
            case PROGRESS:
                binding.toolbar.setTitle("Progress Tracking");
                break;
        }
        
        refreshCurrentView();
        
        // Load adherence history when switching to progress view
        if (mode == ViewMode.PROGRESS && assignmentId != -1) {
            viewModel.loadNutritionAdherenceHistory(String.valueOf(assignmentId));
        }
    }
    
    private void refreshCurrentView() {
        if (currentPlan == null) return;
        
        switch (currentViewMode) {
            case TODAY:
                displayTodaysMeals(currentPlan);
                break;
            case WEEKLY:
                displayWeeklyMeals(currentPlan);
                break;
            case PROGRESS:
                displayProgressView();
                break;
        }
    }

    private void displayTodaysMeals(DetailedNutritionPlan plan) {
        // Set today's date in header
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        String todayText = "Today - " + dateFormat.format(new Date());
        binding.textViewTodayDate.setText(todayText);
        
        // Always hide stats card in today view
        binding.cardViewStats.setVisibility(View.GONE);
        
        // Get today's weekday
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        // Convert to our enum format
        DetailedNutritionPlanDay.Weekday todayWeekday = getTodayWeekday(dayOfWeek);
        
        // Find today's nutrition plan day
        DetailedNutritionPlanDay todaysDay = null;
        for (DetailedNutritionPlanDay day : plan.getDays()) {
            if (day.getWeekday() == todayWeekday) {
                todaysDay = day;
                break;
            }
        }

        if (todaysDay != null) {
            todayMealsAdapter.updateMeals(todaysDay.getMeals());
            updateDayMacros(todaysDay);
            updateEmptyState(todaysDay.getMeals().isEmpty());
            
            // Update adapter in RecyclerView
            binding.recyclerViewMeals.setAdapter(todayMealsAdapter);
            binding.recyclerViewMeals.setVisibility(View.VISIBLE);
        } else {
            todayMealsAdapter.updateMeals(new ArrayList<>());
            binding.textViewDayMacros.setText("No plan for today");
            updateEmptyState(true);
        }
    }

    private DetailedNutritionPlanDay.Weekday getTodayWeekday(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return DetailedNutritionPlanDay.Weekday.sun;
            case Calendar.MONDAY: return DetailedNutritionPlanDay.Weekday.mon;
            case Calendar.TUESDAY: return DetailedNutritionPlanDay.Weekday.tue;
            case Calendar.WEDNESDAY: return DetailedNutritionPlanDay.Weekday.wed;
            case Calendar.THURSDAY: return DetailedNutritionPlanDay.Weekday.thu;
            case Calendar.FRIDAY: return DetailedNutritionPlanDay.Weekday.fri;
            case Calendar.SATURDAY: return DetailedNutritionPlanDay.Weekday.sat;
            default: return DetailedNutritionPlanDay.Weekday.mon;
        }
    }

    private void updateDayMacros(DetailedNutritionPlanDay day) {
        StringBuilder macros = new StringBuilder("Daily Target: ");
        
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
        
        binding.textViewDayMacros.setText(macros.toString());
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.textViewEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewMeals.setVisibility(View.GONE);
        } else {
            binding.textViewEmptyState.setVisibility(View.GONE);
            binding.recyclerViewMeals.setVisibility(View.VISIBLE);
        }
    }
    
    private void displayWeeklyMeals(DetailedNutritionPlan plan) {
        // Update header for weekly view
        binding.textViewTodayDate.setText("Weekly Nutrition Plan Overview");
        
        // Always hide stats card in weekly view
        binding.cardViewStats.setVisibility(View.GONE);
        
        weeklyMealsAdapter.updatePlan(plan);
        binding.recyclerViewMeals.setAdapter(weeklyMealsAdapter);
        binding.recyclerViewMeals.setVisibility(View.VISIBLE);
        
        // Update header for weekly view
        updateWeeklyMacros(plan);
        updateEmptyState(plan.getDays().isEmpty());
    }
    
    private void displayProgressView() {
        // Update header for progress view
        binding.textViewTodayDate.setText("Track your nutrition adherence over time");
        binding.textViewDayMacros.setText("Nutrition Adherence Progress");
        
        binding.recyclerViewMeals.setAdapter(adherenceAdapter);
        binding.recyclerViewMeals.setVisibility(View.VISIBLE);
        binding.cardViewStats.setVisibility(View.VISIBLE);
        
        // Check if we have adherence data
        List<NutritionAdherenceHistory> currentData = viewModel.nutritionAdherenceHistory.getValue();
        if (currentData == null || currentData.isEmpty()) {
            binding.textViewEmptyState.setText("No adherence data available yet. Complete some meals to see your progress!");
            binding.textViewEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewMeals.setVisibility(View.GONE);
            
            // Initialize with empty stats
            binding.textViewWeeklyAdherence.setText("Weekly Adherence: N/A");
            binding.textViewMonthlyAverage.setText("Monthly Average: N/A");
            binding.textViewTotalMealsCompleted.setText("Meals Completed: 0/0");
        } else {
            binding.textViewEmptyState.setVisibility(View.GONE);
            binding.recyclerViewMeals.setVisibility(View.VISIBLE);
            adherenceAdapter.updateAdherence(currentData);
            updateProgressStats(currentData);
        }
    }
    
    private void updateWeeklyMacros(DetailedNutritionPlan plan) {
        int totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;
        int daysCount = 0;
        
        for (DetailedNutritionPlanDay day : plan.getDays()) {
            if (day.getTotalCalories() != null) {
                totalCalories += day.getTotalCalories();
                daysCount++;
            }
            if (day.getProtein() != null) totalProtein += day.getProtein().doubleValue();
            if (day.getCarbs() != null) totalCarbs += day.getCarbs().doubleValue();
            if (day.getFat() != null) totalFat += day.getFat().doubleValue();
        }
        
        StringBuilder macros = new StringBuilder("Weekly Average: ");
        if (daysCount > 0) {
            macros.append(totalCalories / daysCount).append(" cal/day");
            macros.append(" | P: ").append(Math.round(totalProtein / daysCount)).append("g");
            macros.append(" | C: ").append(Math.round(totalCarbs / daysCount)).append("g");
            macros.append(" | F: ").append(Math.round(totalFat / daysCount)).append("g");
        } else {
            macros.append("No data available");
        }
        
        binding.textViewDayMacros.setText(macros.toString());
    }
    
    private void updateProgressStats(List<NutritionAdherenceHistory> adherenceHistory) {
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
            NutritionAdherenceHistory day = adherenceHistory.get(i);
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
            NutritionAdherenceHistory day = adherenceHistory.get(i);
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

    private void updateEditButtonVisibility(DetailedNutritionPlan plan) {
        String currentUserId = viewModel.currentUserId.getValue();
        boolean isCreatedByCurrentUser = plan.getCreatedBy() != null && 
                                       plan.getCreatedBy().equals(currentUserId);
        binding.buttonEdit.setVisibility(isCreatedByCurrentUser ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCompleteMeal(DetailedNutritionPlanMeal meal) {
        showMealCompletionDialog(meal);
    }

    private void showMealCompletionDialog(DetailedNutritionPlanMeal meal) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_complete_meal, null);
        
        TextInputEditText editTextNotes = dialogView.findViewById(R.id.editTextNotes);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Complete " + meal.getName())
                .setView(dialogView)
                .setPositiveButton("Complete", (dialog, which) -> {
                    String notes = null;
                    if (editTextNotes.getText() != null && !editTextNotes.getText().toString().trim().isEmpty()) {
                        notes = editTextNotes.getText().toString().trim();
                    }
                    
                    MealCompletion completion = new MealCompletion(notes);
                    viewModel.completeMeal(String.valueOf(assignmentId), String.valueOf(meal.getId()), completion);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from edit activity
        loadPlanDetails();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}