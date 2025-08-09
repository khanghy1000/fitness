package com.example.fitness.ui.activity;

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
import com.example.fitness.data.network.model.generated.NutritionPlanDay;
import com.example.fitness.databinding.ActivityNutritionPlanDetailsBinding;
import com.example.fitness.ui.adapter.NutritionDayAdapter;
import com.example.fitness.ui.viewmodel.NutritionPlanDetailsViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NutritionPlanDetailsActivity extends AppCompatActivity implements NutritionDayAdapter.OnDayClickListener {

    private ActivityNutritionPlanDetailsBinding binding;
    private NutritionPlanDetailsViewModel viewModel;
    private NutritionDayAdapter dayAdapter;
    private String planId;
    private String planName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityNutritionPlanDetailsBinding.inflate(getLayoutInflater());
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
            viewModel.loadNutritionPlanDetails(planId);
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        planId = String.valueOf(intent.getIntExtra("PLAN_ID", -1));
        planName = intent.getStringExtra("PLAN_NAME");
        
        if (planId.equals("-1")) {
            Toast.makeText(this, "Invalid plan ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void initializeViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(planName != null ? planName : "Nutrition Plan Details");
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NutritionPlanDetailsViewModel.class);
    }

    private void setupRecyclerView() {
        dayAdapter = new NutritionDayAdapter(this);
        binding.recyclerViewDays.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewDays.setAdapter(dayAdapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.fabEditPlan.setOnClickListener(v -> {
            Intent intent = new Intent(this, NutritionPlanEditActivity.class);
            intent.putExtra("PLAN_ID", Integer.parseInt(planId));
            intent.putExtra("PLAN_NAME", planName);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        viewModel.detailedNutritionPlan.observe(this, this::displayNutritionPlan);

        viewModel.nutritionPlanDays.observe(this, nutritionPlanDays -> {
            if (nutritionPlanDays != null) {
                dayAdapter.updateDays(nutritionPlanDays);
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

    private void displayNutritionPlan(DetailedNutritionPlan detailedNutritionPlan) {
        if (detailedNutritionPlan == null) return;
        
        binding.textViewPlanName.setText(detailedNutritionPlan.getName());
        
        if (detailedNutritionPlan.getDescription() != null && !detailedNutritionPlan.getDescription().isEmpty()) {
            binding.textViewPlanDescription.setText(detailedNutritionPlan.getDescription());
            binding.textViewPlanDescription.setVisibility(View.VISIBLE);
        } else {
            binding.textViewPlanDescription.setVisibility(View.GONE);
        }

        // Set status chip
        binding.chipPlanStatus.setText(detailedNutritionPlan.isActive() ? "Active" : "Inactive");
        binding.chipPlanStatus.setChecked(detailedNutritionPlan.isActive());

        // Format and display created date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(detailedNutritionPlan.getCreatedAt());
            binding.textViewCreatedDate.setText("Created: " + outputFormat.format(date));
        } catch (Exception e) {
            binding.textViewCreatedDate.setText("Created: " + detailedNutritionPlan.getCreatedAt());
        }
    }

    @Override
    public void onDayClick(NutritionPlanDay day) {
        // Handle day click - could show day details or edit day
        Toast.makeText(this, "Day: " + day.getWeekday().getValue(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from edit activity
        if (planId != null) {
            viewModel.refreshPlanDetails(planId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}