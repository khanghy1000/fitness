package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.DetailedNutritionPlan;
import com.example.fitness.data.network.model.generated.DetailedUser;
import com.example.fitness.data.network.model.generated.MealCompletion;
import com.example.fitness.data.network.model.generated.MealCompletionResponse;
import com.example.fitness.data.network.model.generated.NutritionPlanAssignment;
import com.example.fitness.data.network.model.generated.WorkoutPlanAssignment;
import com.example.fitness.data.repository.AuthRepository;
import com.example.fitness.data.repository.NutritionRepository;
import com.example.fitness.data.repository.UsersRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class TraineeNutritionPlanViewModel extends ViewModel {
    private final UsersRepository usersRepository;
    private final NutritionRepository nutritionRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<List<NutritionPlanAssignment>> _nutritionPlanAssignments = new MutableLiveData<>();
    public final LiveData<List<NutritionPlanAssignment>> nutritionPlanAssignments = _nutritionPlanAssignments;

    private final MutableLiveData<DetailedNutritionPlan> _detailedNutritionPlan = new MutableLiveData<>();
    public final LiveData<DetailedNutritionPlan> detailedNutritionPlan = _detailedNutritionPlan;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _isRefreshing = new MutableLiveData<>();
    public final LiveData<Boolean> isRefreshing = _isRefreshing;

    private final MutableLiveData<String> _successMessage = new MutableLiveData<>();
    public final LiveData<String> successMessage = _successMessage;

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
    public TraineeNutritionPlanViewModel(UsersRepository usersRepository, NutritionRepository nutritionRepository, AuthRepository authRepository) {
        this.usersRepository = usersRepository;
        this.nutritionRepository = nutritionRepository;
        this.authRepository = authRepository;
        loadUserNutritionPlans();
        loadCurrentUserId();
    }

    public void loadUserNutritionPlans() {
        _isLoading.setValue(true);
        usersRepository.getUserNutritionPlans(new UsersRepository.UsersCallback<List<NutritionPlanAssignment>>() {
            @Override
            public void onSuccess(List<NutritionPlanAssignment> result) {
                _isLoading.setValue(false);
                _isRefreshing.setValue(false);

                List<NutritionPlanAssignment> activeAssignments = new ArrayList<>();
                for (NutritionPlanAssignment assignment : result) {
                    if (assignment.getStatus() == NutritionPlanAssignment.Status.active) {
                        activeAssignments.add(assignment);
                    }
                }

                _nutritionPlanAssignments.setValue(activeAssignments);
                _error.setValue(null);
                
                // Fetch creator details for all plans
                fetchCreatorDetailsForAssignments(activeAssignments);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _isRefreshing.setValue(false);
                _error.setValue(error);
            }
        });
    }

    private void fetchCreatorDetailsForAssignments(List<NutritionPlanAssignment> assignments) {
        List<String> creatorIdsToFetch = new ArrayList<>();
        
        for (NutritionPlanAssignment assignment : assignments) {
            if (assignment.getNutritionPlan() != null && assignment.getNutritionPlan().getCreatedBy() != null) {
                String creatorId = assignment.getNutritionPlan().getCreatedBy();
                
                // Skip if we already have this creator's name or are currently fetching it
                if (!creatorNamesCache.containsKey(creatorId) && !fetchingCreatorIds.contains(creatorId)) {
                    fetchingCreatorIds.add(creatorId);
                    creatorIdsToFetch.add(creatorId);
                }
            }
        }
        
        android.util.Log.d("CreatorDebug", "Will fetch " + creatorIdsToFetch.size() + " creator IDs: " + creatorIdsToFetch);
        
        // Fetch all creator names
        for (String creatorId : creatorIdsToFetch) {
            fetchCreatorName(creatorId, creatorIdsToFetch.size());
        }
    }

    private void fetchCreatorName(String creatorId, int totalToFetch) {
        android.util.Log.d("CreatorDebug", "Fetching creator for ID: " + creatorId);
        usersRepository.getUserById(creatorId, new UsersRepository.UsersCallback<DetailedUser>() {
            @Override
            public void onSuccess(DetailedUser result) {
                synchronized (TraineeNutritionPlanViewModel.this) {
                    fetchingCreatorIds.remove(creatorId);
                    creatorNamesCache.put(creatorId, result.getName());
                    
                    android.util.Log.d("CreatorDebug", "Success for ID: " + creatorId + ", Name: " + result.getName() + ", Cache size: " + creatorNamesCache.size() + ", Still fetching: " + fetchingCreatorIds.size());
                    
                    // Update LiveData with the accumulated cache
                    Map<String, String> updatedMap = new HashMap<>(creatorNamesCache);
                    _creatorNames.postValue(updatedMap);
                }
            }

            @Override
            public void onError(String error) {
                synchronized (TraineeNutritionPlanViewModel.this) {
                    fetchingCreatorIds.remove(creatorId);
                    creatorNamesCache.put(creatorId, "User #" + creatorId);
                    
                    android.util.Log.d("CreatorDebug", "Error for ID: " + creatorId + ", Error: " + error + ", Cache size: " + creatorNamesCache.size() + ", Still fetching: " + fetchingCreatorIds.size());
                    
                    // Update LiveData with the accumulated cache
                    Map<String, String> updatedMap = new HashMap<>(creatorNamesCache);
                    _creatorNames.postValue(updatedMap);
                }
            }
        });
    }

    public void refreshNutritionPlans() {
        _isRefreshing.setValue(true);
        loadUserNutritionPlans();
    }

    public void loadNutritionPlanDetails(String planId) {
        _isLoading.setValue(true);
        nutritionRepository.getNutritionPlanById(planId, new NutritionRepository.NutritionCallback<DetailedNutritionPlan>() {
            @Override
            public void onSuccess(DetailedNutritionPlan result) {
                _isLoading.setValue(false);
                _detailedNutritionPlan.setValue(result);
                _error.setValue(null);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _error.setValue(error);
            }
        });
    }

    public void completeMeal(String userNutritionPlanId, String mealId, MealCompletion mealCompletion) {
        _isLoading.setValue(true);
        usersRepository.completeMeal(userNutritionPlanId, mealId, mealCompletion, new UsersRepository.UsersCallback<MealCompletionResponse>() {
            @Override
            public void onSuccess(MealCompletionResponse result) {
                _isLoading.setValue(false);
                _successMessage.setValue("Meal completed successfully!");
                _error.setValue(null);
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

    public void clearSuccessMessage() {
        _successMessage.setValue(null);
    }

    private void loadCurrentUserId() {
        authRepository.getCurrentUserIdSync()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                userId -> _currentUserId.setValue(userId),
                throwable -> {
                    // Handle error if needed
                    android.util.Log.e("TraineeNutritionPlanViewModel", "Failed to get current user ID", throwable);
                }
            );
    }
}
