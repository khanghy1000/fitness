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
import com.example.fitness.data.network.model.generated.NutritionPlan;
import com.example.fitness.databinding.ActivityCoachNutritionPlanBinding;
import com.example.fitness.ui.activity.NutritionPlanDetailsActivity;
import com.example.fitness.ui.activity.NutritionPlanEditActivity;
import com.example.fitness.ui.adapter.NutritionPlanAdapter;
import com.example.fitness.ui.viewmodel.NutritionPlanViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CoachNutritionPlanActivity extends AppCompatActivity implements NutritionPlanAdapter.OnNutritionPlanClickListener {

    private ActivityCoachNutritionPlanBinding binding;
    private NutritionPlanViewModel viewModel;
    private NutritionPlanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityCoachNutritionPlanBinding.inflate(getLayoutInflater());
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
        viewModel = new ViewModelProvider(this).get(NutritionPlanViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new NutritionPlanAdapter(this);
        binding.recyclerViewNutritionPlans.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewNutritionPlans.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.fabAddNutritionPlan.setOnClickListener(v -> showCreatePlanDialog());
        
        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshNutritionPlans());
    }

    private void observeViewModel() {
        viewModel.nutritionPlans.observe(this, nutritionPlans -> {
            if (nutritionPlans != null) {
                adapter.updateNutritionPlans(nutritionPlans);
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
                Toast.makeText(this, "Nutrition plan created successfully", Toast.LENGTH_SHORT).show();
                viewModel.clearCreatedPlan();
            }
        });
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
    public void onNutritionPlanClick(NutritionPlan nutritionPlan) {
        Intent intent = new Intent(this, NutritionPlanDetailsActivity.class);
        intent.putExtra("PLAN_ID", nutritionPlan.getId());
        intent.putExtra("PLAN_NAME", nutritionPlan.getName());
        startActivity(intent);
    }

    @Override
    public void onNutritionPlanOptionsClick(NutritionPlan nutritionPlan, View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.menu_nutrition_plan_options, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                editNutritionPlan(nutritionPlan);
                return true;
            } else if (itemId == R.id.action_delete) {
                showDeleteConfirmationDialog(nutritionPlan);
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void editNutritionPlan(NutritionPlan nutritionPlan) {
        Intent intent = new Intent(this, NutritionPlanEditActivity.class);
        intent.putExtra("PLAN_ID", nutritionPlan.getId());
        intent.putExtra("PLAN_NAME", nutritionPlan.getName());
        startActivity(intent);
    }

    private void showDeleteConfirmationDialog(NutritionPlan nutritionPlan) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Nutrition Plan")
                .setMessage("Are you sure you want to delete \"" + nutritionPlan.getName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteNutritionPlan(String.valueOf(nutritionPlan.getId()));
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