package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.CreateWorkoutPlan;
import com.example.fitness.data.network.model.generated.DetailedUser;
import com.example.fitness.data.network.model.generated.WorkoutPlan;
import com.example.fitness.data.network.model.generated.WorkoutPlanAssignment;
import com.example.fitness.data.network.model.generated.WorkoutPlanAssignmentResponse;
import com.example.fitness.data.network.model.generated.AssignWorkoutPlan;
import com.example.fitness.data.repository.AuthRepository;
import com.example.fitness.data.repository.UsersRepository;
import com.example.fitness.data.repository.WorkoutsRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class WorkoutPlanViewModel extends ViewModel {
    private final UsersRepository usersRepository;
    private final WorkoutsRepository workoutsRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<List<WorkoutPlan>> _workoutPlans = new MutableLiveData<>();
    public final LiveData<List<WorkoutPlan>> workoutPlans = _workoutPlans;

    private final MutableLiveData<List<WorkoutPlanAssignment>> _userWorkoutPlanAssignments = new MutableLiveData<>();
    public final LiveData<List<WorkoutPlanAssignment>> userWorkoutPlanAssignments = _userWorkoutPlanAssignments;

    private final MutableLiveData<List<WorkoutPlanAssignment>> _activeWorkoutPlanAssignments = new MutableLiveData<>();
    public final LiveData<List<WorkoutPlanAssignment>> activeWorkoutPlanAssignments = _activeWorkoutPlanAssignments;

    private final MutableLiveData<List<WorkoutPlanAssignment>> _completedWorkoutPlanAssignments = new MutableLiveData<>();
    public final LiveData<List<WorkoutPlanAssignment>> completedWorkoutPlanAssignments = _completedWorkoutPlanAssignments;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _isRefreshing = new MutableLiveData<>();
    public final LiveData<Boolean> isRefreshing = _isRefreshing;

    private final MutableLiveData<WorkoutPlan> _createdPlan = new MutableLiveData<>();
    public final LiveData<WorkoutPlan> createdPlan = _createdPlan;

    private final MutableLiveData<String> _successMessage = new MutableLiveData<>();
    public final LiveData<String> successMessage = _successMessage;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    // Cache for creator details
    private final MutableLiveData<Map<String, String>> _creatorNames = new MutableLiveData<>(new HashMap<>());
    public final LiveData<Map<String, String>> creatorNames = _creatorNames;
    
    // Track which creator IDs are currently being fetched to prevent duplicate requests
    private final Set<String> fetchingCreatorIds = new HashSet<>();
    
    // Local map to accumulate creator names before posting to LiveData
    private final Map<String, String> creatorNamesCache = new HashMap<>();

    private final MutableLiveData<String> _currentUserId = new MutableLiveData<>();
    public final LiveData<String> currentUserId = _currentUserId;

    @Inject
    public WorkoutPlanViewModel(UsersRepository usersRepository, WorkoutsRepository workoutsRepository, AuthRepository authRepository) {
        this.usersRepository = usersRepository;
        this.workoutsRepository = workoutsRepository;
        this.authRepository = authRepository;
        loadWorkoutPlans();
        loadCurrentUserId();
    }

    public void loadWorkoutPlans() {
        _isLoading.setValue(true);
        workoutsRepository.getAllWorkoutPlans(new WorkoutsRepository.WorkoutsCallback<List<WorkoutPlan>>() {
            @Override
            public void onSuccess(List<WorkoutPlan> result) {
                _isLoading.setValue(false);
                _isRefreshing.setValue(false);
                _workoutPlans.setValue(result);
                _error.setValue(null);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _isRefreshing.setValue(false);
                _error.setValue(error);
            }
        });
    }

    // Load trainee workout plan assignments
    public void loadUserWorkoutPlanAssignments() {
        _isLoading.setValue(true);
        usersRepository.getUserWorkoutPlans(new UsersRepository.UsersCallback<List<WorkoutPlanAssignment>>() {
            @Override
            public void onSuccess(List<WorkoutPlanAssignment> result) {
                _isLoading.setValue(false);
                _isRefreshing.setValue(false);
                
                // Separate active and completed workout plan assignments
                List<WorkoutPlanAssignment> activeAssignments = new ArrayList<>();
                List<WorkoutPlanAssignment> completedAssignments = new ArrayList<>();
                
                for (WorkoutPlanAssignment assignment : result) {
                    if (assignment.getStatus() == WorkoutPlanAssignment.Status.active) {
                        activeAssignments.add(assignment);
                    } else if (assignment.getStatus() == WorkoutPlanAssignment.Status.completed) {
                        completedAssignments.add(assignment);
                    }
                }
                
                _userWorkoutPlanAssignments.setValue(result);
                _activeWorkoutPlanAssignments.setValue(activeAssignments);
                _completedWorkoutPlanAssignments.setValue(completedAssignments);
                _error.setValue(null);
                
                // Fetch creator details for all plans
                fetchCreatorDetailsForAssignments(result);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _isRefreshing.setValue(false);
                _error.setValue(error);
            }
        });
    }

    private void fetchCreatorDetailsForAssignments(List<WorkoutPlanAssignment> assignments) {
        List<String> creatorIdsToFetch = new ArrayList<>();
        
        for (WorkoutPlanAssignment assignment : assignments) {
            if (assignment.getWorkoutPlan() != null && assignment.getWorkoutPlan().getCreatedBy() != null) {
                String creatorId = assignment.getWorkoutPlan().getCreatedBy();
                
                // Skip if we already have this creator's name or are currently fetching it
                if (!creatorNamesCache.containsKey(creatorId) && !fetchingCreatorIds.contains(creatorId)) {
                    fetchingCreatorIds.add(creatorId);
                    creatorIdsToFetch.add(creatorId);
                }
            }
        }
        
        android.util.Log.d("CreatorDebug", "Workout - Will fetch " + creatorIdsToFetch.size() + " creator IDs: " + creatorIdsToFetch);
        
        // Fetch all creator names
        for (String creatorId : creatorIdsToFetch) {
            fetchCreatorName(creatorId, creatorIdsToFetch.size());
        }
    }

    private void fetchCreatorName(String creatorId, int totalToFetch) {
        android.util.Log.d("CreatorDebug", "Workout - Fetching creator for ID: " + creatorId);
        usersRepository.getUserById(creatorId, new UsersRepository.UsersCallback<DetailedUser>() {
            @Override
            public void onSuccess(DetailedUser result) {
                synchronized (WorkoutPlanViewModel.this) {
                    fetchingCreatorIds.remove(creatorId);
                    creatorNamesCache.put(creatorId, result.getName());
                    
                    android.util.Log.d("CreatorDebug", "Workout - Success for ID: " + creatorId + ", Name: " + result.getName() + ", Cache size: " + creatorNamesCache.size() + ", Still fetching: " + fetchingCreatorIds.size());
                    
                    // Update LiveData with the accumulated cache
                    Map<String, String> updatedMap = new HashMap<>(creatorNamesCache);
                    _creatorNames.postValue(updatedMap);
                }
            }

            @Override
            public void onError(String error) {
                synchronized (WorkoutPlanViewModel.this) {
                    fetchingCreatorIds.remove(creatorId);
                    creatorNamesCache.put(creatorId, "User #" + creatorId);
                    
                    android.util.Log.d("CreatorDebug", "Workout - Error for ID: " + creatorId + ", Error: " + error + ", Cache size: " + creatorNamesCache.size() + ", Still fetching: " + fetchingCreatorIds.size());
                    
                    // Update LiveData with the accumulated cache
                    Map<String, String> updatedMap = new HashMap<>(creatorNamesCache);
                    _creatorNames.postValue(updatedMap);
                }
            }
        });
    }

    public void refreshWorkoutPlans() {
        _isRefreshing.setValue(true);
        loadWorkoutPlans();
    }

    // Refresh trainee workout plan assignments
    public void refreshUserWorkoutPlanAssignments() {
        _isRefreshing.setValue(true);
        loadUserWorkoutPlanAssignments();
    }

    // Refresh active workout plan assignments
    public void refreshActiveWorkoutPlanAssignments() {
        _isRefreshing.setValue(true);
        loadUserWorkoutPlanAssignments();
    }

    // Refresh completed workout plan assignments
    public void refreshCompletedWorkoutPlanAssignments() {
        _isRefreshing.setValue(true);
        loadUserWorkoutPlanAssignments();
    }

    public void createWorkoutPlan(String name, String description, CreateWorkoutPlan.Difficulty difficulty) {
        _isLoading.setValue(true);
        
        CreateWorkoutPlan createWorkoutPlan = new CreateWorkoutPlan(name, description, difficulty, null);
        
        workoutsRepository.createWorkoutPlan(createWorkoutPlan, new WorkoutsRepository.WorkoutsCallback<WorkoutPlan>() {
            @Override
            public void onSuccess(WorkoutPlan result) {
                _isLoading.setValue(false);
                _createdPlan.setValue(result);
                _error.setValue(null);
                // Refresh the list
                loadWorkoutPlans();
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _error.setValue(error);
            }
        });
    }

    public void clearError() {
        _error.setValue(null);
    }

    public void clearCreatedPlan() {
        _createdPlan.setValue(null);
    }

    public void assignWorkoutPlan(String workoutPlanId, String traineeId) {
        _isLoading.setValue(true);
        
        // Use current date as start date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = dateFormat.format(new Date());
        AssignWorkoutPlan assignWorkoutPlan = new AssignWorkoutPlan(traineeId, startDate);
        
        usersRepository.assignWorkoutPlan(workoutPlanId, assignWorkoutPlan, new UsersRepository.UsersCallback<WorkoutPlanAssignmentResponse>() {
            @Override
            public void onSuccess(WorkoutPlanAssignmentResponse result) {
                _isLoading.setValue(false);
                _successMessage.setValue("Workout plan assigned successfully!");
                _errorMessage.setValue(null);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
                _successMessage.setValue(null);
            }
        });
    }

    public void clearMessages() {
        _successMessage.setValue(null);
        _errorMessage.setValue(null);
    }

    private void loadCurrentUserId() {
        authRepository.getCurrentUserIdSync()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                userId -> _currentUserId.setValue(userId),
                throwable -> {
                    // Handle error if needed
                    android.util.Log.e("WorkoutPlanViewModel", "Failed to get current user ID", throwable);
                }
            );
    }
}
