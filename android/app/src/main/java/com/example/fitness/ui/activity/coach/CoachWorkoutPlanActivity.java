package com.example.fitness.ui.activity.coach;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.CreateWorkoutPlan;
import com.example.fitness.data.network.model.generated.WorkoutPlan;
import com.example.fitness.databinding.ActivityCoachWorkoutPlanBinding;
import com.example.fitness.ui.adapter.WorkoutPlanAdapter;
import com.example.fitness.ui.dialog.CreateWorkoutPlanDialogFragment;
import com.example.fitness.ui.viewmodel.WorkoutPlanViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CoachWorkoutPlanActivity extends AppCompatActivity implements WorkoutPlanAdapter.OnWorkoutPlanClickListener {

    private ActivityCoachWorkoutPlanBinding binding;
    private WorkoutPlanViewModel viewModel;
    private WorkoutPlanAdapter workoutPlanAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityCoachWorkoutPlanBinding.inflate(getLayoutInflater());
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
    }

    private void setupRecyclerView() {
        workoutPlanAdapter = new WorkoutPlanAdapter(this);
        binding.recyclerViewWorkoutPlans.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewWorkoutPlans.setAdapter(workoutPlanAdapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.fabAddWorkoutPlan.setOnClickListener(v -> showCreateWorkoutPlanDialog());
        
        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshWorkoutPlans());
    }

    private void observeViewModel() {
        viewModel.workoutPlans.observe(this, workoutPlans -> {
            if (workoutPlans != null && !workoutPlans.isEmpty()) {
                binding.textViewEmpty.setVisibility(View.GONE);
                binding.recyclerViewWorkoutPlans.setVisibility(View.VISIBLE);
                workoutPlanAdapter.updateWorkoutPlans(workoutPlans);
            } else {
                binding.textViewEmpty.setVisibility(View.VISIBLE);
                binding.recyclerViewWorkoutPlans.setVisibility(View.GONE);
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.isRefreshing.observe(this, isRefreshing -> {
            binding.swipeRefreshLayout.setRefreshing(isRefreshing);
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
        Intent intent = new Intent(this, CoachWorkoutPlanDetailsActivity.class);
        intent.putExtra("PLAN_ID", workoutPlan.getId());
        intent.putExtra("PLAN_NAME", workoutPlan.getName());
        startActivity(intent);
    }

    @Override
    public void onWorkoutPlanOptionsClick(WorkoutPlan workoutPlan, View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.menu_workout_plan_options, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_view_details) {
                onWorkoutPlanClick(workoutPlan);
                return true;
            } else if (itemId == R.id.action_edit) {
                Intent intent = new Intent(this, CoachWorkoutPlanEditActivity.class);
                intent.putExtra("PLAN_ID", workoutPlan.getId());
                intent.putExtra("PLAN_NAME", workoutPlan.getName());
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_delete) {
                // TODO: Implement delete functionality
                Toast.makeText(this, "Delete functionality coming soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}