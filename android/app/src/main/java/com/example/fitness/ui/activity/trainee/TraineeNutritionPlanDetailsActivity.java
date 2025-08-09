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
import com.example.fitness.data.network.model.generated.DetailedNutritionPlan;
import com.example.fitness.data.network.model.generated.DetailedNutritionPlanDay;
import com.example.fitness.data.network.model.generated.DetailedNutritionPlanMeal;
import com.example.fitness.data.network.model.generated.MealCompletion;
import com.example.fitness.databinding.ActivityTraineeNutritionPlanDetailsBinding;
import com.example.fitness.ui.adapter.TodayMealsAdapter;
import com.example.fitness.ui.viewmodel.TraineeNutritionPlanViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
    private TodayMealsAdapter adapter;
    
    private int assignmentId;
    private int planId;
    private String planName;
    private DetailedNutritionPlan currentPlan;

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
        
        // Set today's date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        String todayText = "Today - " + dateFormat.format(new Date());
        binding.textViewTodayDate.setText(todayText);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TraineeNutritionPlanViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new TodayMealsAdapter(this);
        binding.recyclerViewTodayMeals.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewTodayMeals.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.swipeRefreshLayout.setOnRefreshListener(() -> loadPlanDetails());
    }

    private void observeViewModel() {
        viewModel.detailedNutritionPlan.observe(this, plan -> {
            if (plan != null) {
                currentPlan = plan;
                displayTodaysMeals(plan);
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
        if (planId != -1) {
            viewModel.loadNutritionPlanDetails(String.valueOf(planId));
        }
    }

    private void displayTodaysMeals(DetailedNutritionPlan plan) {
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
            adapter.updateMeals(todaysDay.getMeals());
            updateDayMacros(todaysDay);
            updateEmptyState(todaysDay.getMeals().isEmpty());
        } else {
            adapter.updateMeals(new ArrayList<>());
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
            macros.append(" | P: ").append(day.getProtein()).append("g");
        }
        if (day.getCarbs() != null) {
            macros.append(" | C: ").append(day.getCarbs()).append("g");
        }
        if (day.getFat() != null) {
            macros.append(" | F: ").append(day.getFat()).append("g");
        }
        
        binding.textViewDayMacros.setText(macros.toString());
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.textViewEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewTodayMeals.setVisibility(View.GONE);
        } else {
            binding.textViewEmptyState.setVisibility(View.GONE);
            binding.recyclerViewTodayMeals.setVisibility(View.VISIBLE);
        }
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
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}