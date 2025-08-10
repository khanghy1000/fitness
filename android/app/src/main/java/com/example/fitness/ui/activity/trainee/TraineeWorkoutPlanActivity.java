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
import com.example.fitness.data.network.model.generated.WorkoutPlan;
import com.example.fitness.databinding.ActivityTraineeWorkoutPlanBinding;
import com.example.fitness.ui.activity.WorkoutPlanEditActivity;
import com.example.fitness.ui.adapter.TraineeWorkoutPlanAdapter;
import com.example.fitness.ui.dialog.CreateWorkoutPlanDialogFragment;
import com.example.fitness.ui.viewmodel.WorkoutPlanViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TraineeWorkoutPlanActivity extends AppCompatActivity implements TraineeWorkoutPlanAdapter.OnWorkoutPlanClickListener {

    private ActivityTraineeWorkoutPlanBinding binding;
    private WorkoutPlanViewModel viewModel;
    private TraineeWorkoutPlanAdapter workoutPlanAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityTraineeWorkoutPlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupViewModel();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void initializeViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WorkoutPlanViewModel.class);
        viewModel.loadUserWorkoutPlanAssignments();
    }

    private void setupRecyclerView() {
        workoutPlanAdapter = new TraineeWorkoutPlanAdapter(this);
        binding.recyclerViewWorkoutPlans.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewWorkoutPlans.setAdapter(workoutPlanAdapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.fabAddWorkoutPlan.setOnClickListener(v -> showCreateWorkoutPlanDialog());
        
        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshUserWorkoutPlanAssignments());
    }

    private void observeViewModel() {
        viewModel.userWorkoutPlanAssignments.observe(this, workoutPlanAssignments -> {
            if (workoutPlanAssignments != null && !workoutPlanAssignments.isEmpty()) {
                binding.textViewEmpty.setVisibility(View.GONE);
                binding.recyclerViewWorkoutPlans.setVisibility(View.VISIBLE);
                workoutPlanAdapter.updateWorkoutPlanAssignments(workoutPlanAssignments);
            } else {
                binding.textViewEmpty.setVisibility(View.VISIBLE);
                binding.recyclerViewWorkoutPlans.setVisibility(View.GONE);
            }
        });

        viewModel.creatorNames.observe(this, creatorNames -> {
            if (creatorNames != null) {
                workoutPlanAdapter.updateCreatorNames(creatorNames);
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.isRefreshing.observe(this, isRefreshing -> {
            binding.swipeRefreshLayout.setRefreshing(isRefreshing);
        });

        viewModel.currentUserId.observe(this, currentUserId -> {
            if (currentUserId != null) {
                workoutPlanAdapter.setCurrentUserId(currentUserId);
            }
        });

        viewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });

        viewModel.createdPlan.observe(this, createdPlan -> {
            if (createdPlan != null) {
                Toast.makeText(this, "Workout plan created successfully", Toast.LENGTH_SHORT).show();
                viewModel.clearCreatedPlan();
            }
        });
    }

    private void showCreateWorkoutPlanDialog() {
        CreateWorkoutPlanDialogFragment dialog = new CreateWorkoutPlanDialogFragment();
        dialog.setOnWorkoutPlanCreateListener((name, description, difficulty) -> {
            viewModel.createWorkoutPlan(name, description, difficulty);
        });
        dialog.show(getSupportFragmentManager(), "CreateWorkoutPlanDialog");
    }

    @Override
    public void onWorkoutPlanClick(WorkoutPlan workoutPlan) {
        Intent intent = new Intent(this, TraineeWorkoutPlanDetailsActivity.class);
        intent.putExtra("PLAN_ID", workoutPlan.getId());
        intent.putExtra("PLAN_NAME", workoutPlan.getName());
        startActivity(intent);
    }

    @Override
    public void onWorkoutPlanEdit(WorkoutPlan workoutPlan) {
        Intent intent = new Intent(this, WorkoutPlanEditActivity.class);
        intent.putExtra("PLAN_ID", workoutPlan.getId());
        intent.putExtra("PLAN_NAME", workoutPlan.getName());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}