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
import com.example.fitness.data.network.model.generated.NutritionPlanAssignment;
import com.example.fitness.databinding.ActivityTraineeNutritionPlanBinding;
import com.example.fitness.ui.adapter.TraineeNutritionPlanAssignmentAdapter;
import com.example.fitness.ui.viewmodel.TraineeNutritionPlanViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TraineeNutritionPlanActivity extends AppCompatActivity implements TraineeNutritionPlanAssignmentAdapter.OnAssignmentClickListener {

    private ActivityTraineeNutritionPlanBinding binding;
    private TraineeNutritionPlanViewModel viewModel;
    private TraineeNutritionPlanAssignmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityTraineeNutritionPlanBinding.inflate(getLayoutInflater());
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
        viewModel = new ViewModelProvider(this).get(TraineeNutritionPlanViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new TraineeNutritionPlanAssignmentAdapter(this);
        binding.recyclerViewNutritionPlans.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewNutritionPlans.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshNutritionPlans());
    }

    private void observeViewModel() {
        viewModel.nutritionPlanAssignments.observe(this, assignments -> {
            if (assignments != null) {
                adapter.updateAssignments(assignments);
                updateEmptyState(assignments.isEmpty());
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
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.textViewEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewNutritionPlans.setVisibility(View.GONE);
        } else {
            binding.textViewEmptyState.setVisibility(View.GONE);
            binding.recyclerViewNutritionPlans.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAssignmentClick(NutritionPlanAssignment assignment) {
        Intent intent = new Intent(this, TraineeNutritionPlanDetailsActivity.class);
        intent.putExtra("ASSIGNMENT_ID", assignment.getId());
        intent.putExtra("PLAN_ID", assignment.getNutritionPlanId());
        intent.putExtra("PLAN_NAME", assignment.getNutritionPlan() != null ? 
            assignment.getNutritionPlan().getName() : "Nutrition Plan #" + assignment.getNutritionPlanId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}