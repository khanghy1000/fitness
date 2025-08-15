package com.example.fitness.ui.activity.trainee;

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
import com.example.fitness.data.network.model.generated.NutritionPlanAssignment;
import com.example.fitness.databinding.ActivityTraineeNutritionPlanBinding;
import com.example.fitness.ui.activity.NutritionPlanEditActivity;
import com.example.fitness.ui.adapter.TraineeNutritionPlanAssignmentAdapter;
import com.example.fitness.ui.viewmodel.TraineeNutritionPlanViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
        
        binding.fabAddNutritionPlan.setOnClickListener(v -> showCreatePlanDialog());
    }

    private void observeViewModel() {
        viewModel.nutritionPlanAssignments.observe(this, assignments -> {
            if (assignments != null) {
                adapter.updateAssignments(assignments);
                updateEmptyState(assignments.isEmpty());
            }
        });

        viewModel.creatorNames.observe(this, creatorNames -> {
            if (creatorNames != null) {
                adapter.updateCreatorNames(creatorNames);
            }
        });

        viewModel.currentUserId.observe(this, currentUserId -> {
            if (currentUserId != null) {
                adapter.setCurrentUserId(currentUserId);
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

        viewModel.successMessage.observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                viewModel.clearSuccessMessage();
            }
        });

        viewModel.createdPlan.observe(this, createdPlan -> {
            if (createdPlan != null) {
                Toast.makeText(this, "Nutrition plan created successfully", Toast.LENGTH_SHORT).show();
                viewModel.clearCreatedPlan();
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
    public void onAssignmentEdit(NutritionPlanAssignment assignment) {
        Intent intent = new Intent(this, NutritionPlanEditActivity.class);
        intent.putExtra("PLAN_ID", assignment.getNutritionPlanId());
        intent.putExtra("PLAN_NAME", assignment.getNutritionPlan() != null ? 
            assignment.getNutritionPlan().getName() : "Nutrition Plan #" + assignment.getNutritionPlanId());
        startActivity(intent);
    }

    private void showCreatePlanDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_nutrition_plan, null);
        TextInputLayout nameInputLayout = dialogView.findViewById(R.id.textInputLayoutPlanName);
        TextInputLayout descriptionInputLayout = dialogView.findViewById(R.id.textInputLayoutPlanDescription);
        TextInputEditText nameEditText = dialogView.findViewById(R.id.editTextPlanName);
        TextInputEditText descriptionEditText = dialogView.findViewById(R.id.editTextPlanDescription);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Create New Nutrition Plan")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = nameEditText.getText() != null ? nameEditText.getText().toString().trim() : "";
                    String description = descriptionEditText.getText() != null ? descriptionEditText.getText().toString().trim() : "";
                    
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Plan name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    viewModel.createNutritionPlan(name, description.isEmpty() ? null : description);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onAssignmentOptions(NutritionPlanAssignment assignment, View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.menu_nutrition_plan_options, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                editNutritionPlan(assignment);
                return true;
            } else if (itemId == R.id.action_delete) {
                showDeleteConfirmationDialog(assignment);
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void editNutritionPlan(NutritionPlanAssignment assignment) {
        Intent intent = new Intent(this, NutritionPlanEditActivity.class);
        intent.putExtra("PLAN_ID", assignment.getNutritionPlanId());
        intent.putExtra("PLAN_NAME", assignment.getNutritionPlan() != null ? 
            assignment.getNutritionPlan().getName() : "Nutrition Plan #" + assignment.getNutritionPlanId());
        startActivity(intent);
    }

    private void showDeleteConfirmationDialog(NutritionPlanAssignment assignment) {
        String planName = assignment.getNutritionPlan() != null ? 
            assignment.getNutritionPlan().getName() : "Nutrition Plan #" + assignment.getNutritionPlanId();
            
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Nutrition Plan")
                .setMessage("Are you sure you want to delete \"" + planName + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteNutritionPlan(String.valueOf(assignment.getNutritionPlanId()));
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