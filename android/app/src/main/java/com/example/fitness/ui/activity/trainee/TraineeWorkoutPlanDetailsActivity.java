package com.example.fitness.ui.activity.trainee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.fitness.data.network.model.generated.DetailedWorkoutPlanDay;
import com.example.fitness.databinding.ActivityTraineeWorkoutPlanDetailsBinding;
import com.example.fitness.ui.activity.WorkoutPlanEditActivity;
import com.example.fitness.ui.adapter.TraineeWorkoutPlanDayAdapter;
import com.example.fitness.ui.viewmodel.WorkoutPlanDetailsViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TraineeWorkoutPlanDetailsActivity extends AppCompatActivity implements TraineeWorkoutPlanDayAdapter.OnDayClickListener {

    private static final String TAG = "TraineeWorkoutActivity";
    private ActivityTraineeWorkoutPlanDetailsBinding binding;
    private WorkoutPlanDetailsViewModel viewModel;
    private TraineeWorkoutPlanDayAdapter dayAdapter;
    private String planId;
    private String planName;
    private DetailedWorkoutPlan currentPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityTraineeWorkoutPlanDetailsBinding.inflate(getLayoutInflater());
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
        
        // Load workout plan data
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
            getSupportActionBar().setTitle("Workout Plan Details");
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WorkoutPlanDetailsViewModel.class);
    }

    private void setupRecyclerView() {
        dayAdapter = new TraineeWorkoutPlanDayAdapter();
        dayAdapter.setOnDayClickListener(this);
        binding.recyclerViewDays.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewDays.setAdapter(dayAdapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkoutPlanEditActivity.class);
            intent.putExtra("PLAN_ID", Integer.parseInt(planId));
            intent.putExtra("PLAN_NAME", planName);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        viewModel.detailedWorkoutPlan.observe(this, plan -> {
            this.currentPlan = plan;
            this.displayWorkoutPlan(plan);
            updateEditButtonVisibility(plan);
        });

        viewModel.currentUserId.observe(this, currentUserId -> {
            if (currentUserId != null && currentPlan != null) {
                updateEditButtonVisibility(currentPlan);
            }
        });

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

        // Calories
        if (detailedWorkoutPlan.getEstimatedCalories() != null) {
            binding.textViewCalories.setText("~" + detailedWorkoutPlan.getEstimatedCalories() + " cal");
            binding.textViewCalories.setVisibility(View.VISIBLE);
        } else {
            binding.textViewCalories.setVisibility(View.GONE);
        }

        // Days
        if (detailedWorkoutPlan.getWorkoutPlanDays() != null && !detailedWorkoutPlan.getWorkoutPlanDays().isEmpty()) {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.recyclerViewDays.setVisibility(View.VISIBLE);
            dayAdapter.updateDays(detailedWorkoutPlan.getWorkoutPlanDays());
        } else {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.recyclerViewDays.setVisibility(View.GONE);
        }
    }

    private void updateEditButtonVisibility(DetailedWorkoutPlan plan) {
        String currentUserId = viewModel.currentUserId.getValue();
        boolean isCreatedByCurrentUser = plan != null && plan.getCreatedBy() != null && 
                                       plan.getCreatedBy().equals(currentUserId);
        binding.buttonEdit.setVisibility(isCreatedByCurrentUser ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from edit activity
        if (planId != null) {
            viewModel.loadWorkoutPlan(planId);
        }
    }

    @Override
    public void onDayClick(DetailedWorkoutPlanDay day) {
        Intent intent = new Intent(this, TraineeWorkoutPlanDayDetailsActivity.class);
        intent.putExtra("DAY_ID", day.getId());
        intent.putExtra("DAY_NUMBER", day.getDay());
        intent.putExtra("IS_REST_DAY", day.isRestDay());
        intent.putExtra("PLAN_NAME", planName);
        intent.putExtra("PLAN_ID", planId);
        intent.putExtra("DAY_DURATION", day.getDuration());
        intent.putExtra("DAY_CALORIES", day.getEstimatedCalories());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}