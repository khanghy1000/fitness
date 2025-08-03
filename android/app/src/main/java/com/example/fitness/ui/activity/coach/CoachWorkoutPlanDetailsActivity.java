package com.example.fitness.ui.activity.coach;

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
import com.example.fitness.data.network.model.generated.DetailedWorkoutPlan;
import com.example.fitness.data.repository.ExercisesRepository;
import com.example.fitness.databinding.ActivityCoachWorkoutPlanDetailsBinding;
import com.example.fitness.ui.adapter.WorkoutDayDetailAdapter;
import com.example.fitness.ui.viewmodel.WorkoutPlanDetailsViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CoachWorkoutPlanDetailsActivity extends AppCompatActivity {

    private ActivityCoachWorkoutPlanDetailsBinding binding;
    private WorkoutPlanDetailsViewModel viewModel;
    private WorkoutDayDetailAdapter dayAdapter;
    private String planId;
    private String planName;

    @Inject
    ExercisesRepository exercisesRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityCoachWorkoutPlanDetailsBinding.inflate(getLayoutInflater());
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
        
        if (planId != null) {
            viewModel.loadWorkoutPlan(planId);
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        int planIdInt = intent.getIntExtra("PLAN_ID", -1);
        planName = intent.getStringExtra("PLAN_NAME");
        
        if (planIdInt != -1) {
            planId = String.valueOf(planIdInt);
        }
    }

    private void initializeViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(planName != null ? planName : "Workout Plan Details");
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WorkoutPlanDetailsViewModel.class);
    }

    private void setupRecyclerView() {
        dayAdapter = new WorkoutDayDetailAdapter();
        dayAdapter.setExercisesRepository(exercisesRepository);
        binding.recyclerViewDays.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewDays.setAdapter(dayAdapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.fabEditWorkoutPlan.setOnClickListener(v -> {
            Intent intent = new Intent(this, CoachWorkoutPlanEditActivity.class);
            intent.putExtra("PLAN_ID", Integer.parseInt(planId));
            intent.putExtra("PLAN_NAME", planName);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        viewModel.detailedWorkoutPlan.observe(this, this::displayWorkoutPlan);

        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
    }

    private void displayWorkoutPlan(DetailedWorkoutPlan detailedWorkoutPlan) {
        if (detailedWorkoutPlan == null) {
            return;
        }
        
        // Plan info
        binding.textViewPlanName.setText(detailedWorkoutPlan.getName());
        
        if (detailedWorkoutPlan.getDescription() != null && !detailedWorkoutPlan.getDescription().isEmpty()) {
            binding.textViewPlanDescription.setText(detailedWorkoutPlan.getDescription());
            binding.textViewPlanDescription.setVisibility(View.VISIBLE);
        } else {
            binding.textViewPlanDescription.setVisibility(View.GONE);
        }

        // Difficulty
        if (detailedWorkoutPlan.getDifficulty() != null) {
            String difficulty = detailedWorkoutPlan.getDifficulty().getValue();
            binding.chipDifficulty.setText(difficulty.substring(0, 1).toUpperCase() + difficulty.substring(1));
            binding.chipDifficulty.setVisibility(View.VISIBLE);
        } else {
            binding.chipDifficulty.setVisibility(View.GONE);
        }

        // Status
        binding.chipStatus.setText(detailedWorkoutPlan.isActive() ? "Active" : "Inactive");
        binding.chipStatus.setChecked(detailedWorkoutPlan.isActive());

        // Calories
        if (detailedWorkoutPlan.getEstimatedCalories() != null) {
            binding.textViewCalories.setText("~" + detailedWorkoutPlan.getEstimatedCalories() + " cal");
            binding.textViewCalories.setVisibility(View.VISIBLE);
        } else {
            binding.textViewCalories.setVisibility(View.GONE);
        }

        // Days
        if (detailedWorkoutPlan.getWorkoutPlanDays() != null && !detailedWorkoutPlan.getWorkoutPlanDays().isEmpty()) {
            dayAdapter.updateDays(detailedWorkoutPlan.getWorkoutPlanDays());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}