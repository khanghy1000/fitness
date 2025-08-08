package com.example.fitness.ui.activity.trainee;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitness.R;
import com.example.fitness.ble.BleServiceManager;
import com.example.fitness.data.network.model.generated.DetailedWorkoutPlanDayExercise;
import com.example.fitness.data.network.model.generated.ExerciseType;
import com.example.fitness.data.repository.ExercisesRepository;
import com.example.fitness.databinding.ActivityTraineeWorkoutPlanDayDetailsBinding;
import com.example.fitness.ui.adapter.TraineeWorkoutExerciseAdapter;
import com.example.fitness.ui.dialog.BleConnectionDialog;
import com.example.fitness.ui.viewmodel.WorkoutDayDetailsViewModel;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TraineeWorkoutPlanDayDetailsActivity extends AppCompatActivity {

    private ActivityTraineeWorkoutPlanDayDetailsBinding binding;
    private TraineeWorkoutExerciseAdapter exerciseAdapter;
    private WorkoutDayDetailsViewModel viewModel;
    private BleServiceManager bleServiceManager;
    
    private int dayId;
    private int dayNumber;
    private boolean isRestDay;
    private String planName;
    private String planId;
    private Integer dayDuration;
    private Integer dayCalories;
    private List<DetailedWorkoutPlanDayExercise> currentExercises;
    
    @Inject
    ExercisesRepository exercisesRepository;
    
    // Permission request launcher
    private final ActivityResultLauncher<String[]> permissionLauncher = 
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean allGranted = true;
            for (Boolean granted : result.values()) {
                if (!granted) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                showBleConnectionDialog();
            } else {
                Toast.makeText(this, "Bluetooth permissions are required for rep counting exercises", Toast.LENGTH_LONG).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityTraineeWorkoutPlanDayDetailsBinding.inflate(getLayoutInflater());
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
        displayDayInfo();
        
        // Initialize BLE service manager
        bleServiceManager = new BleServiceManager(this);
        
        // Load exercises from the plan
        if (planId != null && !isRestDay) {
            viewModel.loadWorkoutPlanAndExtractDay(planId, dayNumber);
        }
    }

    private void getIntentData() {
        if (getIntent() != null) {
            dayId = getIntent().getIntExtra("DAY_ID", 0);
            dayNumber = getIntent().getIntExtra("DAY_NUMBER", 1);
            isRestDay = getIntent().getBooleanExtra("IS_REST_DAY", false);
            planName = getIntent().getStringExtra("PLAN_NAME");
            planId = getIntent().getStringExtra("PLAN_ID");
            dayDuration = getIntent().getIntExtra("DAY_DURATION", 0);
            dayCalories = getIntent().getIntExtra("DAY_CALORIES", 0);
        }
    }

    private void initializeViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Day " + dayNumber);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WorkoutDayDetailsViewModel.class);
    }

    private void setupRecyclerView() {
        exerciseAdapter = new TraineeWorkoutExerciseAdapter();
        exerciseAdapter.setExercisesRepository(exercisesRepository);
        binding.recyclerViewExercises.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewExercises.setAdapter(exerciseAdapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.buttonStart.setOnClickListener(v -> {
            // Check if workout has reps exercises that require BLE connection
            if (hasRepsExercises()) {
                checkPermissionsAndConnect();
            } else {
                startWorkout();
            }
        });
    }
    
    private boolean hasRepsExercises() {
        if (currentExercises == null) return false;
        
        for (DetailedWorkoutPlanDayExercise exercise : currentExercises) {
            if (exercise.getExerciseType().getLogType() == ExerciseType.LogType.reps) {
                return true;
            }
        }
        return false;
    }
    
    private void checkPermissionsAndConnect() {
        String[] requiredPermissions = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        };
        
        boolean allPermissionsGranted = true;
        for (String permission : requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }
        
        if (allPermissionsGranted) {
            showBleConnectionDialog();
        } else {
            permissionLauncher.launch(requiredPermissions);
        }
    }
    
    private void showBleConnectionDialog() {
        BleConnectionDialog dialog = BleConnectionDialog.newInstance();
        dialog.setBleServiceManager(bleServiceManager);
        dialog.setBleConnectionDialogListener(new BleConnectionDialog.BleConnectionDialogListener() {
            @Override
            public void onBleConnected() {
                startWorkout();
            }
            
            @Override
            public void onBleConnectionFailed(String error) {
                Toast.makeText(TraineeWorkoutPlanDayDetailsActivity.this, 
                    "Failed to connect to exercise tracker: " + error, Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onBleConnectionCancelled() {
                Toast.makeText(TraineeWorkoutPlanDayDetailsActivity.this, 
                    "Connection cancelled. Automatic rep counting will not be available.", Toast.LENGTH_LONG).show();
            }
        });
        dialog.show(getSupportFragmentManager(), "ble_connection");
    }
    
    private void startWorkout() {
        Intent intent = new Intent(this, TraineeWorkoutActivity.class);
        intent.putExtra("DAY_ID", dayId);
        intent.putExtra("DAY_NUMBER", dayNumber);
        intent.putExtra("PLAN_NAME", planName);
        intent.putExtra("PLAN_ID", planId);
        
        // For now, we'll use a placeholder user workout plan ID
        // In a real app, this would come from the actual user assignment
        intent.putExtra("USER_WORKOUT_PLAN_ID", "1");
        
        // Pass BLE connection status
        intent.putExtra("BLE_CONNECTED", bleServiceManager != null && bleServiceManager.isConnected());
        
        startActivity(intent);
    }

    private void observeViewModel() {
        viewModel.exercises.observe(this, this::updateExercises);

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

    private void displayDayInfo() {
        // Day title
        binding.textViewDayTitle.setText("Day " + dayNumber);
        
        // Plan name
        if (planName != null) {
            binding.textViewPlanName.setText(planName);
        }
        
        if (isRestDay) {
            // Show rest day layout
            binding.layoutRestDay.setVisibility(View.VISIBLE);
            binding.layoutWorkoutInfo.setVisibility(View.GONE);
            binding.textViewExercisesTitle.setVisibility(View.GONE);
            binding.recyclerViewExercises.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.buttonStart.setVisibility(View.GONE);
        } else {
            // Show workout day layout
            binding.layoutRestDay.setVisibility(View.GONE);
            binding.layoutWorkoutInfo.setVisibility(View.VISIBLE);
            binding.textViewExercisesTitle.setVisibility(View.VISIBLE);
            binding.recyclerViewExercises.setVisibility(View.VISIBLE);
            binding.buttonStart.setVisibility(View.VISIBLE);
            
            // Display actual data from intent
            int durationInMinutes = dayDuration != null ? dayDuration / 60 : 0;
            binding.textViewDuration.setText(durationInMinutes + " min");
            binding.textViewCalories.setText("~" + (dayCalories != null ? dayCalories : 0) + " cal");
            binding.textViewExerciseCount.setText("0 exercises"); // Will be updated when exercises load
        }
    }

    // Method to update exercises when data is loaded
    public void updateExercises(List<DetailedWorkoutPlanDayExercise> exercises) {
        this.currentExercises = exercises; // Store for BLE check
        
        if (exercises != null && !exercises.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.recyclerViewExercises.setVisibility(View.VISIBLE);
            exerciseAdapter.updateExercises(exercises);
            
            // Update exercise count
            binding.textViewExerciseCount.setText(exercises.size() + " exercises");
            
            // Calculate total duration and calories
            int totalDuration = 0;
            int totalCalories = 0;
            
            for (DetailedWorkoutPlanDayExercise exercise : exercises) {
                if (exercise.getTargetDuration() != null) {
                    totalDuration += exercise.getTargetDuration();
                }
                if (exercise.getEstimatedCalories() != null) {
                    totalCalories += exercise.getEstimatedCalories();
                }
            }
            
            // Update duration and calories if not already set from intent
            if (dayDuration == null || dayDuration == 0) {
                binding.textViewDuration.setText((totalDuration / 60) + " min");
            }
            if (dayCalories == null || dayCalories == 0) {
                binding.textViewCalories.setText("~" + totalCalories + " cal");
            }
        } else {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.recyclerViewExercises.setVisibility(View.GONE);
            binding.textViewExerciseCount.setText("0 exercises");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleServiceManager != null) {
            bleServiceManager.cleanup();
        }
        binding = null;
    }
}