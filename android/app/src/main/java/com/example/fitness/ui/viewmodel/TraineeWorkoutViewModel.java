package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.DetailedWorkoutPlanDayExercise;
import com.example.fitness.data.network.model.generated.ExerciseResult;
import com.example.fitness.data.network.model.generated.ExerciseType;
import com.example.fitness.data.network.model.generated.RecordExerciseResult;
import com.example.fitness.data.repository.UsersRepository;
import com.example.fitness.ui.viewmodel.WorkoutDayDetailsViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TraineeWorkoutViewModel extends ViewModel {
    private final UsersRepository usersRepository;
    
    private final MutableLiveData<List<DetailedWorkoutPlanDayExercise>> _exercises = new MutableLiveData<>();
    public final LiveData<List<DetailedWorkoutPlanDayExercise>> exercises = _exercises;
    
    private final MutableLiveData<Integer> _currentExerciseIndex = new MutableLiveData<>();
    public final LiveData<Integer> currentExerciseIndex = _currentExerciseIndex;
    
    private final MutableLiveData<Boolean> _isResting = new MutableLiveData<>();
    public final LiveData<Boolean> isResting = _isResting;
    
    private final MutableLiveData<Integer> _restTimeRemaining = new MutableLiveData<>();
    public final LiveData<Integer> restTimeRemaining = _restTimeRemaining;
    
    private final MutableLiveData<Integer> _exerciseTimeRemaining = new MutableLiveData<>();
    public final LiveData<Integer> exerciseTimeRemaining = _exerciseTimeRemaining;
    
    private final MutableLiveData<Integer> _currentReps = new MutableLiveData<>();
    public final LiveData<Integer> currentReps = _currentReps;
    
    private final MutableLiveData<Boolean> _isPaused = new MutableLiveData<>();
    public final LiveData<Boolean> isPaused = _isPaused;
    
    private final MutableLiveData<Boolean> _isWorkoutCompleted = new MutableLiveData<>();
    public final LiveData<Boolean> isWorkoutCompleted = _isWorkoutCompleted;
    
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;
    
    private final MutableLiveData<Boolean> _isRecordingResult = new MutableLiveData<>();
    public final LiveData<Boolean> isRecordingResult = _isRecordingResult;

    // For tracking workout session
    private String userWorkoutPlanId;
    private boolean allowRecording = true; // Default to allow recording
    
    @Inject
    public TraineeWorkoutViewModel(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
        
        // Initialize default values
        _currentExerciseIndex.setValue(-1); // Start at -1, will be incremented to 0 for first exercise
        _isResting.setValue(false);
        _restTimeRemaining.setValue(10); // 10 seconds rest between exercises
        _currentReps.setValue(0);
        _isPaused.setValue(false);
        _isWorkoutCompleted.setValue(false);
        _isRecordingResult.setValue(false);
    }
    
    public void setExercises(List<DetailedWorkoutPlanDayExercise> exercises) {
        _exercises.setValue(exercises);
        _currentExerciseIndex.setValue(-1); // Start at -1 so first exercise is at index 0
        
        // Start with rest period before first exercise
        if (exercises != null && !exercises.isEmpty()) {
            startRestPeriod();
        }
    }
    
    public void setUserWorkoutPlanId(String userWorkoutPlanId) {
        this.userWorkoutPlanId = userWorkoutPlanId;
    }
    
    public void setAllowRecording(boolean allowRecording) {
        this.allowRecording = allowRecording;
    }
    
    public DetailedWorkoutPlanDayExercise getCurrentExercise() {
        List<DetailedWorkoutPlanDayExercise> exerciseList = _exercises.getValue();
        Integer currentIndex = _currentExerciseIndex.getValue();
        
        if (exerciseList != null && currentIndex != null && 
            currentIndex >= 0 && currentIndex < exerciseList.size()) {
            return exerciseList.get(currentIndex);
        }
        return null;
    }
    
    public DetailedWorkoutPlanDayExercise getNextExercise() {
        List<DetailedWorkoutPlanDayExercise> exerciseList = _exercises.getValue();
        Integer currentIndex = _currentExerciseIndex.getValue();
        
        if (exerciseList != null && currentIndex != null) {
            int nextIndex = currentIndex + 1;
            if (nextIndex >= 0 && nextIndex < exerciseList.size()) {
                return exerciseList.get(nextIndex);
            }
        }
        return null;
    }
    
    public void startRestPeriod() {
        _isResting.setValue(true);
        _restTimeRemaining.setValue(10); // 10 seconds rest
        _isPaused.setValue(false);
    }
    
    public void skipRestPeriod() {
        _isResting.setValue(false);
        _restTimeRemaining.setValue(0);
        proceedToExercise();
    }
    
    public void skipExercise() {
        // Just move to next exercise without recording result
        proceedToNextExercise();
    }
    
    public void completeExercise() {
        DetailedWorkoutPlanDayExercise currentExercise = getCurrentExercise();
        if (currentExercise != null) {
            if (currentExercise.getExerciseType().getLogType() == ExerciseType.LogType.reps) {
                // For reps, record the target reps (since user can't adjust anymore)
                Integer targetReps = currentExercise.getTargetReps();
                recordCurrentExerciseResult(targetReps, null);
            } else {
                // For duration, record the target duration minus remaining time
                Integer targetDuration = currentExercise.getTargetDuration();
                Integer timeRemaining = _exerciseTimeRemaining.getValue();
                if (targetDuration != null && timeRemaining != null) {
                    int completedDuration = targetDuration - timeRemaining;
                    recordCurrentExerciseResult(null, completedDuration);
                } else {
                    recordCurrentExerciseResult(null, targetDuration);
                }
            }
        }
    }
    
    public void nextExercise() {
        Integer currentIndex = _currentExerciseIndex.getValue();
        List<DetailedWorkoutPlanDayExercise> exerciseList = _exercises.getValue();
        
        if (currentIndex != null && exerciseList != null) {
            int nextIndex = currentIndex + 1;
            
            if (nextIndex >= exerciseList.size()) {
                // Workout completed
                _isWorkoutCompleted.setValue(true);
            } else {
                // Move to next exercise
                _currentExerciseIndex.setValue(nextIndex);
                proceedToExercise();
            }
        }
    }
    
    private void proceedToExercise() {
        _isResting.setValue(false);
        _currentReps.setValue(0);
        _isPaused.setValue(false);
        
        // Set timer for current exercise if it's duration-based
        DetailedWorkoutPlanDayExercise currentExercise = getCurrentExercise();
        if (currentExercise != null && 
            currentExercise.getExerciseType().getLogType() == ExerciseType.LogType.duration) {
            Integer duration = currentExercise.getTargetDuration();
            _exerciseTimeRemaining.setValue(duration != null ? duration : 30);
        }
    }
    
    public void incrementReps() {
        Integer currentReps = _currentReps.getValue();
        if (currentReps != null) {
            _currentReps.setValue(currentReps + 1);
        }
    }
    
    public void decrementReps() {
        Integer currentReps = _currentReps.getValue();
        if (currentReps != null && currentReps > 0) {
            _currentReps.setValue(currentReps - 1);
        }
    }
    
    public void togglePause() {
        Boolean currentPauseState = _isPaused.getValue();
        _isPaused.setValue(currentPauseState != null ? !currentPauseState : true);
    }
    
    public void updateExerciseTimeRemaining(int timeRemaining) {
        _exerciseTimeRemaining.setValue(timeRemaining);
    }
    
    public void updateRestTimeRemaining(int timeRemaining) {
        _restTimeRemaining.setValue(timeRemaining);
    }
    
    private void recordCurrentExerciseResult(Integer reps, Integer duration) {
        DetailedWorkoutPlanDayExercise currentExercise = getCurrentExercise();
        if (currentExercise == null || userWorkoutPlanId == null || !allowRecording) {
            // Skip recording and proceed to next exercise
            proceedToNextExercise();
            return;
        }
        
        _isRecordingResult.setValue(true);
        
        // Calculate estimated calories based on exercise type and duration/reps
        Integer estimatedCalories = null;
        if (currentExercise.getEstimatedCalories() != null) {
            estimatedCalories = currentExercise.getEstimatedCalories();
        }
        
        RecordExerciseResult recordRequest = new RecordExerciseResult(
            currentExercise.getId(),
            Integer.parseInt(userWorkoutPlanId),
            reps,
            duration,
            estimatedCalories
        );
        
        usersRepository.recordExerciseResult(recordRequest, new UsersRepository.UsersCallback<ExerciseResult>() {
            @Override
            public void onSuccess(ExerciseResult result) {
                _isRecordingResult.setValue(false);
                proceedToNextExercise();
            }
            
            @Override
            public void onError(String error) {
                _isRecordingResult.setValue(false);
                _error.setValue("Failed to record exercise result: " + error);
                
                // Continue to next exercise even if recording failed
                proceedToNextExercise();
            }
        });
    }
    
    private void proceedToNextExercise() {
        // Check if this was the last exercise
        Integer currentIndex = _currentExerciseIndex.getValue();
        List<DetailedWorkoutPlanDayExercise> exerciseList = _exercises.getValue();
        
        if (currentIndex != null && exerciseList != null && 
            currentIndex >= exerciseList.size() - 1) {
            // This was the last exercise, complete workout
            _isWorkoutCompleted.setValue(true);
        } else {
            // Start rest period before next exercise
            startRestPeriod();
        }
    }
    
    public void clearError() {
        _error.setValue(null);
    }
    
    public int getTotalExerciseCount() {
        List<DetailedWorkoutPlanDayExercise> exerciseList = _exercises.getValue();
        return exerciseList != null ? exerciseList.size() : 0;
    }
    
    public int getCurrentExerciseNumber() {
        Integer currentIndex = _currentExerciseIndex.getValue();
        return currentIndex != null ? Math.max(currentIndex + 1, 1) : 1;
    }
}
