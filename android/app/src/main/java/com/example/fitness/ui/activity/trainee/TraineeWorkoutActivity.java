package com.example.fitness.ui.activity.trainee;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fitness.R;
import com.example.fitness.ble.BleExerciseCounter;
import com.example.fitness.ble.BleServiceManager;
import com.example.fitness.data.network.model.generated.DetailedWorkoutPlanDayExercise;
import com.example.fitness.data.network.model.generated.ExerciseType;
import com.example.fitness.data.repository.ExercisesRepository;
import com.example.fitness.databinding.ActivityTraineeWorkoutBinding;
import com.example.fitness.ui.viewmodel.TraineeWorkoutViewModel;
import com.example.fitness.ui.viewmodel.WorkoutDayDetailsViewModel;

import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TraineeWorkoutActivity extends AppCompatActivity {

    private static final String TAG = "TraineeWorkoutActivity";
    
    private ActivityTraineeWorkoutBinding binding;
    private TraineeWorkoutViewModel workoutViewModel;
    private WorkoutDayDetailsViewModel dayDetailsViewModel;
    
    @Inject
    ExercisesRepository exercisesRepository;
    
    // Timer handling
    private Handler timerHandler;
    private Runnable exerciseTimerRunnable;
    private Runnable restTimerRunnable;
    
    // BLE functionality
    private BleServiceManager bleServiceManager;
    private BleExerciseCounter exerciseCounter;
    private boolean isBleConnected;
    private int currentRepCount = 0;
    private Integer targetReps = 0;
    private String currentExerciseCounterName = null; // Track which exercise counter is set up
    
    // Intent data
    private int dayId;
    private int dayNumber;
    private String planName;
    private String planId;
    private String userWorkoutPlanId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityTraineeWorkoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getIntentData();
        initializeViews();
        setupViewModels();
        setupBleIfConnected();
        setupListeners();
        observeViewModels();
        
        // Initialize timer handler
        timerHandler = new Handler(Looper.getMainLooper());
        
        // Load exercises for the day
        if (planId != null) {
            // Check if we should start with uncompleted exercises only
            boolean startWithUncompleted = getIntent().getBooleanExtra("START_WITH_UNCOMPLETED", false);
            if (startWithUncompleted) {
                // Set the user workout plan ID for progress tracking
                dayDetailsViewModel.setUserWorkoutPlanId(userWorkoutPlanId);
                dayDetailsViewModel.loadCurrentUserId();
            }
            dayDetailsViewModel.loadWorkoutPlanAndExtractDay(planId, dayNumber);
        }
    }

    private void getIntentData() {
        if (getIntent() != null) {
            dayId = getIntent().getIntExtra("DAY_ID", 0);
            dayNumber = getIntent().getIntExtra("DAY_NUMBER", 1);
            planName = getIntent().getStringExtra("PLAN_NAME");
            planId = getIntent().getStringExtra("PLAN_ID");
            userWorkoutPlanId = getIntent().getStringExtra("USER_WORKOUT_PLAN_ID");
            isBleConnected = getIntent().getBooleanExtra("BLE_CONNECTED", false);
        }
    }

    private void initializeViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Day " + dayNumber + " Workout");
        }
        
        // Hide all layouts initially
        binding.layoutRestPeriod.setVisibility(View.GONE);
        binding.layoutCurrentExercise.setVisibility(View.GONE);
        binding.layoutWorkoutCompleted.setVisibility(View.GONE);
        binding.layoutDurationExercise.setVisibility(View.GONE);
        binding.layoutRepsExercise.setVisibility(View.GONE);
    }

    private void setupViewModels() {
        workoutViewModel = new ViewModelProvider(this).get(TraineeWorkoutViewModel.class);
        dayDetailsViewModel = new ViewModelProvider(this).get(WorkoutDayDetailsViewModel.class);
        
        if (userWorkoutPlanId != null) {
            workoutViewModel.setUserWorkoutPlanId(userWorkoutPlanId);
        }
    }
    
    private void setupBleIfConnected() {
        if (isBleConnected) {
            bleServiceManager = new BleServiceManager(this);
            bleServiceManager.setBleConnectionListener(new BleServiceManager.BleConnectionListener() {
                @Override
                public void onConnectionStatusChanged(boolean isConnected) {
                    isBleConnected = isConnected;
                    if (!isConnected) {
                        Log.d(TAG, "❌ Exercise tracker disconnected");
                        Toast.makeText(TraineeWorkoutActivity.this, 
                            "Exercise tracker disconnected. Manual rep counting will be used.", 
                            Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "🔗 Exercise tracker connected successfully");
                    }
                }
                
                @Override
                public void onDeviceFound(String deviceName, String deviceAddress) {
                    Log.d(TAG, "📱 Found device: " + deviceName + " (" + deviceAddress + ")");
                }
                
                @Override
                public void onDataReceived(String data) {
                    try {
                        // Always log that we received data
                        Log.d(TAG, "📡 Received BLE data: " + data);
                        
                        JSONObject jsonData = new JSONObject(data);
                        if (jsonData.has("predictions")) {
                            if (exerciseCounter != null) {
                                JSONObject predictions = jsonData.getJSONObject("predictions");
                                Log.d(TAG, "🔄 Processing predictions with exercise counter");
                                exerciseCounter.processPrediction(predictions);
                            } else {
                                Log.d(TAG, "⚠️ Received predictions but exercise counter not ready yet");
                            }
                        } else {
                            Log.d(TAG, "⚠️ No 'predictions' field in data");
                        }
                    } catch (Exception e) {
                        // Log JSON parsing errors
                        Log.d(TAG, "❌ Data error: " + e.getMessage());
                        Log.d(TAG, "🔍 Raw data was: " + data);
                    }
                }
                
                @Override
                public void onError(String error) {
                    Log.d(TAG, "❌ BLE Error: " + error);
                    Toast.makeText(TraineeWorkoutActivity.this, "BLE Error: " + error, Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onScanStarted() {
                    Log.d(TAG, "🔍 Scanning for exercise tracker...");
                }
                
                @Override
                public void onScanStopped() {
                    Log.d(TAG, "🛑 Scan stopped");
                }
            });
            
            // Actually start the BLE connection process
            Log.d(TAG, "🚀 Starting BLE connection...");
            if (bleServiceManager.isBluetoothEnabled() && bleServiceManager.hasRequiredPermissions()) {
                bleServiceManager.startScanning();
            } else {
                Log.d(TAG, "❌ Bluetooth not enabled or missing permissions");
                isBleConnected = false;
            }
        }
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        // Duration exercise controls
        binding.buttonPauseResume.setOnClickListener(v -> workoutViewModel.togglePause());
        binding.buttonSkipDuration.setOnClickListener(v -> workoutViewModel.skipExercise());
        
        // Reps exercise controls - removed complete button
        binding.buttonSkipReps.setOnClickListener(v -> workoutViewModel.skipExercise());
        
        // Manual rep counting button
        binding.buttonAddRep.setOnClickListener(v -> {
            currentRepCount++;
            updateRepDisplay();
            
            // Check if target reached
            if (targetReps != null && currentRepCount >= targetReps) {
                Toast.makeText(this, "Target reps reached! Great job!", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Rest period controls
        binding.buttonSkipRest.setOnClickListener(v -> workoutViewModel.skipRestPeriod());
        
        // Workout completion
        binding.buttonFinishWorkout.setOnClickListener(v -> finish());
    }

    private void observeViewModels() {
        // Observe day details to get exercises - prioritize uncompleted exercises
        dayDetailsViewModel.uncompletedExercises.observe(this, uncompletedExercises -> {
            if (uncompletedExercises != null && !uncompletedExercises.isEmpty()) {
                // Start workout with uncompleted exercises
                workoutViewModel.setExercises(uncompletedExercises);
            }
        });
        
        // Fallback to all exercises if uncompleted list is not available
        dayDetailsViewModel.exercises.observe(this, exercises -> {
            if (dayDetailsViewModel.uncompletedExercises.getValue() == null && exercises != null && !exercises.isEmpty()) {
                workoutViewModel.setExercises(exercises);
            }
        });
        
        dayDetailsViewModel.isLoading.observe(this, isLoading -> {
            binding.progressBarLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        dayDetailsViewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                dayDetailsViewModel.clearError();
            }
        });
        
        // Observe workout state
        workoutViewModel.exercises.observe(this, exercises -> {
            if (exercises != null && !exercises.isEmpty()) {
                displayCurrentExercise();
                updateProgress();
            }
        });
        
        workoutViewModel.currentExerciseIndex.observe(this, index -> {
            // Reset exercise counter tracking when moving to new exercise
            currentExerciseCounterName = null;
            displayCurrentExercise();
            updateProgress();
        });
        
        workoutViewModel.isResting.observe(this, isResting -> {
            if (isResting != null) {
                binding.layoutRestPeriod.setVisibility(isResting ? View.VISIBLE : View.GONE);
                binding.layoutCurrentExercise.setVisibility(isResting ? View.GONE : View.VISIBLE);
                
                if (isResting) {
                    displayNextExercisePreview();
                    startRestTimer();
                } else {
                    stopRestTimer();
                    displayCurrentExercise();
                }
            }
        });
        
        workoutViewModel.restTimeRemaining.observe(this, timeRemaining -> {
            if (timeRemaining != null) {
                binding.textViewRestTimer.setText(String.valueOf(timeRemaining));
                
                if (timeRemaining <= 0) {
                    workoutViewModel.nextExercise();
                }
            }
        });
        
        workoutViewModel.exerciseTimeRemaining.observe(this, timeRemaining -> {
            if (timeRemaining != null) {
                binding.textViewDurationTimer.setText(String.valueOf(timeRemaining));
                
                if (timeRemaining <= 0) {
                    workoutViewModel.completeExercise();
                }
            }
        });
        
        workoutViewModel.isPaused.observe(this, isPaused -> {
            if (isPaused != null) {
                binding.buttonPauseResume.setText(isPaused ? "Resume" : "Pause");
                
                if (isPaused) {
                    stopExerciseTimer();
                } else {
                    DetailedWorkoutPlanDayExercise currentExercise = workoutViewModel.getCurrentExercise();
                    if (currentExercise != null && 
                        currentExercise.getExerciseType().getLogType() == ExerciseType.LogType.duration) {
                        startExerciseTimer();
                    }
                }
            }
        });
        
        workoutViewModel.isWorkoutCompleted.observe(this, isCompleted -> {
            if (isCompleted != null && isCompleted) {
                stopAllTimers();
                showWorkoutCompleted();
            }
        });
        
        workoutViewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                workoutViewModel.clearError();
            }
        });
        
        workoutViewModel.isRecordingResult.observe(this, isRecording -> {
            // You can show a loading indicator here if needed
        });
    }

    private void displayCurrentExercise() {
        DetailedWorkoutPlanDayExercise currentExercise = workoutViewModel.getCurrentExercise();
        if (currentExercise == null) {
            return;
        }
        
        binding.layoutCurrentExercise.setVisibility(View.VISIBLE);
        
        // Set exercise name
        binding.textViewExerciseName.setText(currentExercise.getExerciseType().getName());
        
        // Set exercise image
        int imageResource = exercisesRepository.getExerciseImageResourceByName(
            currentExercise.getExerciseType().getName());
        Glide.with(this)
                .asGif()
                .load(imageResource)
                .placeholder(R.drawable.placeholder_exercise)
                .error(R.drawable.placeholder_exercise)
                .into(binding.imageViewExercise);
        
        // Set exercise notes if available
        if (currentExercise.getNotes() != null && !currentExercise.getNotes().isEmpty()) {
            binding.textViewExerciseNotes.setText(currentExercise.getNotes());
            binding.textViewExerciseNotes.setVisibility(View.VISIBLE);
        } else {
            binding.textViewExerciseNotes.setVisibility(View.GONE);
        }
        
        // Show appropriate exercise layout based on log type
        ExerciseType.LogType logType = currentExercise.getExerciseType().getLogType();
        
        if (logType == ExerciseType.LogType.duration) {
            binding.layoutDurationExercise.setVisibility(View.VISIBLE);
            binding.layoutRepsExercise.setVisibility(View.GONE);
            
            // Start duration timer if not paused
            Boolean isPaused = workoutViewModel.isPaused.getValue();
            if (isPaused == null || !isPaused) {
                startExerciseTimer();
            }
            
        } else if (logType == ExerciseType.LogType.reps) {
            binding.layoutDurationExercise.setVisibility(View.GONE);
            binding.layoutRepsExercise.setVisibility(View.VISIBLE);
            
            // Set target reps
            targetReps = currentExercise.getTargetReps();
            binding.textViewTargetReps.setText(String.valueOf(targetReps != null ? targetReps : 0));
            
            // Initialize BLE rep counting if connected
            setupRepCounting(currentExercise);
        }
    }
    
    private void displayNextExercisePreview() {
        DetailedWorkoutPlanDayExercise nextExercise = workoutViewModel.getNextExercise();
        if (nextExercise == null) {
            // Hide the preview if no next exercise
            binding.textViewNextExerciseName.setText("Workout Starting...");
            binding.textViewNextExerciseTarget.setText("");
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.placeholder_exercise)
                    .placeholder(R.drawable.placeholder_exercise)
                    .error(R.drawable.placeholder_exercise)
                    .into(binding.imageViewNextExercise);
            return;
        }
        
        // Set next exercise name
        binding.textViewNextExerciseName.setText(nextExercise.getExerciseType().getName());
        
        // Set next exercise image
        int imageResource = exercisesRepository.getExerciseImageResourceByName(
            nextExercise.getExerciseType().getName());
        Glide.with(this)
                .asGif()
                .load(imageResource)
                .placeholder(R.drawable.placeholder_exercise)
                .error(R.drawable.placeholder_exercise)
                .into(binding.imageViewNextExercise);
        
        // Set target information based on log type
        ExerciseType.LogType logType = nextExercise.getExerciseType().getLogType();
        if (logType == ExerciseType.LogType.duration) {
            Integer duration = nextExercise.getTargetDuration();
            binding.textViewNextExerciseTarget.setText((duration != null ? duration : 30) + " seconds");
        } else if (logType == ExerciseType.LogType.reps) {
            Integer reps = nextExercise.getTargetReps();
            binding.textViewNextExerciseTarget.setText((reps != null ? reps : 0) + " reps");
        }
    }

    private void updateProgress() {
        int currentExerciseNumber = workoutViewModel.getCurrentExerciseNumber();
        int totalExercises = workoutViewModel.getTotalExerciseCount();
        
        binding.textViewExerciseProgress.setText("Exercise " + currentExerciseNumber + " of " + totalExercises);
        
        if (totalExercises > 0) {
            int progress = (int) ((float) currentExerciseNumber / totalExercises * 100);
            binding.progressBarWorkout.setProgress(progress);
        }
    }

    private void startExerciseTimer() {
        stopExerciseTimer(); // Stop any existing timer
        
        exerciseTimerRunnable = new Runnable() {
            @Override
            public void run() {
                Integer currentTime = workoutViewModel.exerciseTimeRemaining.getValue();
                if (currentTime != null && currentTime > 0) {
                    workoutViewModel.updateExerciseTimeRemaining(currentTime - 1);
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        
        timerHandler.postDelayed(exerciseTimerRunnable, 1000);
    }

    private void stopExerciseTimer() {
        if (exerciseTimerRunnable != null) {
            timerHandler.removeCallbacks(exerciseTimerRunnable);
            exerciseTimerRunnable = null;
        }
    }

    private void startRestTimer() {
        stopRestTimer(); // Stop any existing timer
        
        restTimerRunnable = new Runnable() {
            @Override
            public void run() {
                Integer currentTime = workoutViewModel.restTimeRemaining.getValue();
                if (currentTime != null && currentTime > 0) {
                    workoutViewModel.updateRestTimeRemaining(currentTime - 1);
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        
        timerHandler.postDelayed(restTimerRunnable, 1000);
    }

    private void stopRestTimer() {
        if (restTimerRunnable != null) {
            timerHandler.removeCallbacks(restTimerRunnable);
            restTimerRunnable = null;
        }
    }

    private void stopAllTimers() {
        stopExerciseTimer();
        stopRestTimer();
    }
    
    private void setupRepCounting(DetailedWorkoutPlanDayExercise exercise) {
        currentRepCount = 0;
        
        // Check if we already have a counter set up for this exercise
        String exerciseName = exercise.getExerciseType().getName();
        if (exerciseName.equals(currentExerciseCounterName)) {
            Log.d(TAG, "🔄 Rep counter already set up for: " + exerciseName);
            updateRepDisplay();
            return;
        }
        
        if (isBleConnected && bleServiceManager != null) {
            // Get exercise label for BLE matching
            String exerciseLabel = exercisesRepository.getExerciseLabelByName(exercise.getExerciseType().getName());
            Log.d(TAG, "🔍 Looking for exercise label: " + exercise.getExerciseType().getName() + " -> " + exerciseLabel);
            
            if (exerciseLabel != null) {
                // Reset previous counter if exists
                if (exerciseCounter != null) {
                    exerciseCounter.reset();
                }
                
                // Initialize exercise counter for this specific exercise
                Log.d(TAG, "🏗️ Creating exercise counter for: " + exerciseLabel);
                exerciseCounter = new BleExerciseCounter(exerciseLabel);
                currentExerciseCounterName = exerciseName; // Mark this exercise as set up
                exerciseCounter.setRepCountListener(new BleExerciseCounter.RepCountListener() {
                    @Override
                    public void onRepCompleted(int repCount) {
                        runOnUiThread(() -> {
                            currentRepCount = repCount;
                            updateRepDisplay();
                            
                            // Check if target reached
                            if (targetReps != null && repCount >= targetReps) {
                                Toast.makeText(TraineeWorkoutActivity.this, 
                                    "Target reps reached! Great job!", Toast.LENGTH_SHORT).show();
                                // Auto-complete after a brief delay
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    workoutViewModel.completeExercise();
                                }, 2000);
                            }
                        });
                    }
                    
                    @Override
                    public void onExerciseStateChanged(boolean isDoingExercise, String exerciseName, double confidence) {
                        runOnUiThread(() -> {
                            // Update UI to show exercise state
                            if (isDoingExercise) {
                                binding.textViewExerciseName.setTextColor(getColor(com.google.android.material.R.color.design_default_color_primary));
                            } else {
                                binding.textViewExerciseName.setTextColor(getColor(android.R.color.black));
                            }
                        });
                    }
                    
                    @Override
                    public void onDebugLog(String logMessage) {
                        // Log debug messages to logcat
                        Log.d(TAG, logMessage);
                    }
                });
                
                Toast.makeText(this, "BLE rep counting enabled for " + exercise.getExerciseType().getName(), 
                    Toast.LENGTH_SHORT).show();
                
                // Show BLE status
                if (binding.textViewBleStatus != null) {
                    binding.textViewBleStatus.setText("🔗 Automatic rep counting enabled");
                    binding.textViewBleStatus.setVisibility(View.VISIBLE);
                }
                
                Log.d(TAG, "🚀 BLE Rep Counter Started");
                Log.d(TAG, "🔗 Connected to exercise tracker");
                Log.d(TAG, "🏃 Monitoring " + exerciseLabel + " reps...");
                Log.d(TAG, "💡 Start exercising!");
                Log.d(TAG, "------------------------------------");
            } else {
                Log.d(TAG, "❌ No exercise label found for: " + exercise.getExerciseType().getName());
                Log.d(TAG, "📋 Available exercise labels: " + exercisesRepository.getAllExerciseLabels());
                Toast.makeText(this, "BLE rep counting not available for this exercise", 
                    Toast.LENGTH_SHORT).show();
            }
        } else {
            // BLE not connected, show manual mode
            if (binding.textViewBleStatus != null) {
                binding.textViewBleStatus.setText("📱 Manual mode - tap '+1 Rep' to count");
                binding.textViewBleStatus.setVisibility(View.VISIBLE);
            }
            
            // Show manual rep counting button
            binding.buttonAddRep.setVisibility(View.VISIBLE);
        }
        
        updateRepDisplay();
    }
    
    private void updateRepDisplay() {
        // Update current reps display
        if (binding.textViewCurrentReps != null) {
            binding.textViewCurrentReps.setText(String.valueOf(currentRepCount));
        }
        
        // Update progress if we have target reps
        if (targetReps != null && targetReps > 0) {
            float progress = (float) currentRepCount / targetReps;
            if (binding.progressBarReps != null) {
                binding.progressBarReps.setProgress((int) (progress * 100));
            }
        }
    }

    private void showWorkoutCompleted() {
        binding.layoutRestPeriod.setVisibility(View.GONE);
        binding.layoutCurrentExercise.setVisibility(View.GONE);
        binding.layoutWorkoutCompleted.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause timers when activity is paused
        stopAllTimers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume timers if needed
        Boolean isResting = workoutViewModel.isResting.getValue();
        Boolean isPaused = workoutViewModel.isPaused.getValue();
        Boolean isCompleted = workoutViewModel.isWorkoutCompleted.getValue();
        
        if (isCompleted != null && isCompleted) {
            // Don't resume any timers if workout is completed
            return;
        }
        
        if (isResting != null && isResting) {
            startRestTimer();
        } else if (isPaused == null || !isPaused) {
            DetailedWorkoutPlanDayExercise currentExercise = workoutViewModel.getCurrentExercise();
            if (currentExercise != null && 
                currentExercise.getExerciseType().getLogType() == ExerciseType.LogType.duration) {
                startExerciseTimer();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAllTimers();
        
        // Cleanup BLE resources
        if (bleServiceManager != null) {
            bleServiceManager.cleanup();
        }
        if (exerciseCounter != null) {
            exerciseCounter.reset();
        }
        
        binding = null;
    }
}