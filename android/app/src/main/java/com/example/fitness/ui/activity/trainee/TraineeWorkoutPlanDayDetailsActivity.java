package com.example.fitness.ui.activity.trainee;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
import com.example.fitness.ui.activity.WorkoutPlanEditActivity;
import com.example.fitness.ui.adapter.TraineeWorkoutExerciseAdapter;
import com.example.fitness.ui.dialog.BleConnectionDialog;
import com.example.fitness.ui.viewmodel.WorkoutDayDetailsViewModel;
import com.example.fitness.utils.DateUtils;
import com.example.fitness.utils.DurationUtil;

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
    private int assignmentId = -1;
    private String startDate;
    private List<DetailedWorkoutPlanDayExercise> currentExercises;
    private boolean isCompleted = false;
    
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
        
        // Load current user ID and plan data
        viewModel.loadCurrentUserId();
        
        // For demo purposes, set a placeholder userWorkoutPlanId
        // In a real app, this would come from the workout plan assignment
        viewModel.setUserWorkoutPlanId("1");
        
        // Load exercises from the plan
        if (planId != null && !isRestDay) {
            viewModel.loadWorkoutPlanAndExtractDay(planId, dayNumber);
        }
    }

    private void getIntentData() {
        if (getIntent() != null) {
            dayId = getIntent().getIntExtra("DAY_ID", -1);
            dayNumber = getIntent().getIntExtra("DAY_NUMBER", 1);
            isRestDay = getIntent().getBooleanExtra("IS_REST_DAY", false);
            planName = getIntent().getStringExtra("PLAN_NAME");
            planId = getIntent().getStringExtra("PLAN_ID");
            dayDuration = getIntent().getIntExtra("DAY_DURATION", 0);
            dayCalories = getIntent().getIntExtra("DAY_CALORIES", 0);
            assignmentId = getIntent().getIntExtra("ASSIGNMENT_ID", -1);
            startDate = getIntent().getStringExtra("START_DATE");
            isCompleted = getIntent().getBooleanExtra("IS_COMPLETED", false);
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
            // Check if this is a future day
            if (startDate != null && !DateUtils.isDayAvailableForRecording(startDate, dayNumber)) {
                showFutureDayDialog();
            } else {
                // Current or past day - check for BLE if needed
                if (hasRepsExercises()) {
                    checkPermissionsAndConnect();
                } else {
                    startWorkout();
                }
            }
        });
        
        binding.buttonReset.setOnClickListener(v -> {
            // Show confirmation dialog before resetting
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Reset Exercise Progress")
                    .setMessage("Are you sure you want to reset all exercise progress for this day? This action cannot be undone.")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        viewModel.resetExerciseProgress();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        
        binding.buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkoutPlanEditActivity.class);
            intent.putExtra("PLAN_ID", Integer.parseInt(planId));
            intent.putExtra("PLAN_NAME", planName);
            startActivity(intent);
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
                // Determine if recording should be allowed
                boolean allowRecording = startDate == null || DateUtils.isDayAvailableForRecording(startDate, dayNumber);
                startWorkout(allowRecording);
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
    
    
    private void showFutureDayDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Future Workout Day")
                .setMessage("This workout day is scheduled for the future. You can practice the exercises but your results won't be recorded. Do you want to continue?")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Start workout without recording results
                        if (hasRepsExercises()) {
                            checkPermissionsAndConnect();
                        } else {
                            startWorkout(false); // Don't allow recording
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void startWorkout() {
        startWorkout(true); // Allow recording by default
    }
    
    private void startWorkout(boolean allowRecording) {
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
        
        // Pass flag to indicate we should start with uncompleted exercises only
        intent.putExtra("START_WITH_UNCOMPLETED", true);
        
        // Pass whether to allow result recording
        intent.putExtra("ALLOW_RECORDING", allowRecording);
        
        // Pass assignment info for result recording validation
        if (assignmentId != -1) {
            intent.putExtra("ASSIGNMENT_ID", assignmentId);
        }
        if (startDate != null) {
            intent.putExtra("START_DATE", startDate);
        }
        
        startActivity(intent);
    }

    private void updateButtonVisibility(List<DetailedWorkoutPlanDayExercise> uncompletedExercises) {
        if (isRestDay || isCompleted) {
            // Hide workout buttons for rest days or completed workout plans
            binding.buttonStart.setVisibility(View.GONE);
            binding.buttonReset.setVisibility(View.GONE);
            return;
        }

        boolean hasProgress = viewModel.hasExerciseProgress();
        boolean hasUncompletedExercises = uncompletedExercises != null && !uncompletedExercises.isEmpty();

        if (hasUncompletedExercises) {
            // Show start button when there are uncompleted exercises
            binding.buttonStart.setVisibility(View.VISIBLE);
            if (hasProgress) {
                // Show both buttons side by side when there's progress and uncompleted exercises
                binding.buttonStart.setLayoutParams(createWeightedLayoutParams(1, 8));
                binding.buttonReset.setVisibility(View.VISIBLE);
                binding.buttonReset.setLayoutParams(createWeightedLayoutParams(1, 8));
            } else {
                // Show only start button when no progress yet
                binding.buttonStart.setLayoutParams(createWeightedLayoutParams(2, 0));
                binding.buttonReset.setVisibility(View.GONE);
            }
        } else {
            // All exercises completed
            binding.buttonStart.setVisibility(View.GONE);
            if (hasProgress) {
                // Show only reset button at full width when all exercises are completed
                binding.buttonReset.setVisibility(View.VISIBLE);
                binding.buttonReset.setLayoutParams(createWeightedLayoutParams(2, 0));
            } else {
                binding.buttonReset.setVisibility(View.GONE);
            }
        }
    }

    private LinearLayout.LayoutParams createWeightedLayoutParams(int weight, int marginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);
        int marginPx = (int) (marginDp * getResources().getDisplayMetrics().density);
        if (weight == 1) {
            // Side by side buttons
            params.setMarginStart(marginPx);
            params.setMarginEnd(marginPx);
        }
        return params;
    }

    private void updateDayProgress() {
        int completedCount = viewModel.getCompletedExerciseCount();
        int totalCount = viewModel.getTotalExerciseCount();

        if (totalCount > 0 && !isRestDay) {
            binding.layoutDayProgress.setVisibility(View.VISIBLE);
            binding.textViewDayProgress.setText("Progress: " + completedCount + "/" + totalCount + " exercises completed");
            
            int progressPercentage = (completedCount * 100) / totalCount;
            binding.progressBarDayProgress.setProgress(progressPercentage);
        } else {
            binding.layoutDayProgress.setVisibility(View.GONE);
        }
    }

    private void observeViewModel() {
        viewModel.exercises.observe(this, this::updateExercises);

        viewModel.uncompletedExercises.observe(this, uncompletedExercises -> {
            this.updateButtonVisibility(uncompletedExercises);
        });

        viewModel.workoutPlanResults.observe(this, results -> {
            if (results != null) {
                exerciseAdapter.setWorkoutPlanResults(results, dayNumber);
                updateDayProgress();
                // Update button visibility when results change
                this.updateButtonVisibility(viewModel.uncompletedExercises.getValue());
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.isResetting.observe(this, isResetting -> {
            binding.buttonReset.setEnabled(!isResetting);
            binding.buttonReset.setText(isResetting ? "Resetting..." : "Reset Progress");
        });

        viewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });

        viewModel.currentUserId.observe(this, currentUserId -> {
            if (currentUserId != null) {
                checkEditPermission(currentUserId);
            }
        });

        viewModel.currentPlan.observe(this, plan -> {
            if (plan != null && viewModel.currentUserId.getValue() != null) {
                checkEditPermission(viewModel.currentUserId.getValue());
            }
        });
    }

    private void checkEditPermission(String currentUserId) {
        if (viewModel.currentPlan.getValue() != null) {
            String planCreatorId = viewModel.currentPlan.getValue().getCreatedBy();
            boolean canEdit = currentUserId.equals(planCreatorId) && !isCompleted;
            binding.buttonEdit.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        }
    }

    private void displayDayInfo() {
        // Day title with current day indicator
        binding.textViewDayTitle.setText("Day " + dayNumber);
        
        // Show/hide Today chip based on current day, but not for completed plans
        if (!isCompleted && startDate != null && DateUtils.isCurrentDay(startDate, dayNumber)) {
            binding.chipTodayDetails.setVisibility(View.VISIBLE);
            binding.textViewDayTitle.setTextColor(getResources().getColor(R.color.current_day_text));
        } else {
            binding.chipTodayDetails.setVisibility(View.GONE);
            binding.textViewDayTitle.setTextColor(getResources().getColor(android.R.color.black));
        }
        
        // Plan name with completed indicator
        if (planName != null) {
            String planText = planName;
            if (isCompleted) {
                planText += " (Completed)";
            }
            binding.textViewPlanName.setText(planText);
        }
        
        if (isRestDay) {
            // Show rest day layout
            binding.layoutRestDay.setVisibility(View.VISIBLE);
            binding.layoutWorkoutInfo.setVisibility(View.GONE);
            binding.textViewExercisesTitle.setVisibility(View.GONE);
            binding.recyclerViewExercises.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.layoutButtonContainer.setVisibility(View.GONE);
        } else {
            // Show workout day layout
            binding.layoutRestDay.setVisibility(View.GONE);
            binding.layoutWorkoutInfo.setVisibility(View.VISIBLE);
            binding.textViewExercisesTitle.setVisibility(View.VISIBLE);
            binding.recyclerViewExercises.setVisibility(View.VISIBLE);
            // Hide button container for completed plans
            binding.layoutButtonContainer.setVisibility(isCompleted ? View.GONE : View.VISIBLE);
            
            // Display actual data from intent
            int duration = dayDuration != null ? dayDuration : 0;
            binding.textViewDuration.setText(DurationUtil.formatDuration(duration));
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
                binding.textViewDuration.setText(DurationUtil.formatDuration(totalDuration));
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
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from workout activity or edit activity
        if (planId != null && !isRestDay) {
            viewModel.loadWorkoutPlanAndExtractDay(planId, dayNumber);
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