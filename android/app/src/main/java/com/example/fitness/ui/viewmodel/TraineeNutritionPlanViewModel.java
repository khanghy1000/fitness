package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.CreateNutritionPlan;
import com.example.fitness.data.network.model.generated.DetailedNutritionPlan;
import com.example.fitness.data.network.model.generated.DetailedUser;
import com.example.fitness.data.network.model.generated.MealCompletion;
import com.example.fitness.data.network.model.generated.MealCompletionResponse;
import com.example.fitness.data.network.model.generated.DetailedNutritionAdherenceHistory;
import com.example.fitness.data.network.model.generated.NutritionPlan;
import com.example.fitness.data.network.model.generated.NutritionPlanAssignment;
import com.example.fitness.data.network.model.generated.SuccessMessage;
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

    private final MutableLiveData<List<DetailedNutritionAdherenceHistory>> _nutritionAdherenceHistory = new MutableLiveData<>();
    public final LiveData<List<DetailedNutritionAdherenceHistory>> nutritionAdherenceHistory = _nutritionAdherenceHistory;

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

    private final MutableLiveData<NutritionPlan> _createdPlan = new MutableLiveData<>();
    public final LiveData<NutritionPlan> createdPlan = _createdPlan;

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
        usersRepository.getUserNutritionPlans(null, new UsersRepository.UsersCallback<List<NutritionPlanAssignment>>() {
            @Override
            public void onSuccess(List<NutritionPlanAssignment> result) {
                _isLoading.setValue(false);
                _isRefreshing.setValue(false);

                // Show all assignments (active, completed, etc.)
                _nutritionPlanAssignments.setValue(result);
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

    public void refreshNutritionPlanDetails(String planId) {
        // Force refresh plan details without checking cache
        loadNutritionPlanDetails(planId);
    }

    public void refreshNutritionPlanDetailsWithAdherence(String planId, String userNutritionPlanId) {
        // Force refresh both plan details and adherence history
        loadNutritionPlanDetailsWithAdherence(planId, userNutritionPlanId);
    }

    public void loadNutritionPlanDetails(String planId) {
        _isLoading.setValue(true);
        nutritionRepository.getNutritionPlanById(planId, new NutritionRepository.NutritionCallback<DetailedNutritionPlan>() {
            @Override
            public void onSuccess(DetailedNutritionPlan result) {
                _detailedNutritionPlan.setValue(result);
                _error.setValue(null);
                // Keep loading state true until adherence history is also loaded
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _error.setValue(error);
            }
        });
    }

    public void loadNutritionPlanDetailsWithAdherence(String planId, String userNutritionPlanId) {
        android.util.Log.d("ViewModel", "Loading plan details and adherence - planId: " + planId + ", userNutritionPlanId: " + userNutritionPlanId);
        
        _isLoading.setValue(true);
        
        // Load both plan details and adherence history
        nutritionRepository.getNutritionPlanById(planId, new NutritionRepository.NutritionCallback<DetailedNutritionPlan>() {
            @Override
            public void onSuccess(DetailedNutritionPlan result) {
                _detailedNutritionPlan.setValue(result);
                _error.setValue(null);
                
                // Now load adherence history
                String currentUserId = _currentUserId.getValue();
                if (currentUserId != null && userNutritionPlanId != null && !userNutritionPlanId.equals("-1")) {
                    loadNutritionAdherenceHistory(userNutritionPlanId);
                } else {
                    // No adherence history to load, set loading to false
                    _isLoading.setValue(false);
                    _nutritionAdherenceHistory.setValue(new ArrayList<>());
                }
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

    public void loadNutritionAdherenceHistory(String userNutritionPlanId) {
        String currentUserId = _currentUserId.getValue();
        loadNutritionAdherenceHistory(userNutritionPlanId, currentUserId);
    }
    
    public void loadNutritionAdherenceHistory(String userNutritionPlanId, String userId) {
        android.util.Log.d("AdherenceDebug", "Loading adherence history for userNutritionPlanId: " + userNutritionPlanId + ", userId: " + userId);
        
        if (userId == null) {
            android.util.Log.w("AdherenceDebug", "User ID is null, cannot load adherence history");
            _isLoading.setValue(false);
            return;
        }
        
        usersRepository.getNutritionAdherenceHistory(userNutritionPlanId, userId, new UsersRepository.UsersCallback<List<DetailedNutritionAdherenceHistory>>() {
            @Override
            public void onSuccess(List<DetailedNutritionAdherenceHistory> result) {
                android.util.Log.d("AdherenceDebug", "Successfully loaded " + (result != null ? result.size() : 0) + " adherence records");
                _nutritionAdherenceHistory.setValue(result != null ? result : new ArrayList<>());
                _isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("AdherenceDebug", "Failed to load adherence history: " + error);
                _error.setValue("Failed to load adherence history: " + error);
                // Set empty list to show appropriate UI
                _nutritionAdherenceHistory.setValue(new ArrayList<>());
                _isLoading.setValue(false);
            }
        });
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

    public void createNutritionPlan(String name, String description) {
        CreateNutritionPlan createNutritionPlan = new CreateNutritionPlan(name, description);

        nutritionRepository.createNutritionPlan(createNutritionPlan, new NutritionRepository.NutritionCallback<NutritionPlan>() {
            @Override
            public void onSuccess(NutritionPlan result) {
                _createdPlan.setValue(result);
                _successMessage.setValue("Nutrition plan created successfully!");
                // Refresh the list to show the new plan
                loadUserNutritionPlans();
            }

            @Override
            public void onError(String error) {
                _error.setValue("Failed to create nutrition plan: " + error);
            }
        });
    }

    public void deleteNutritionPlan(String planId) {
        nutritionRepository.deleteNutritionPlan(planId, new NutritionRepository.NutritionCallback<SuccessMessage>() {
            @Override
            public void onSuccess(SuccessMessage result) {
                _successMessage.setValue("Nutrition plan deleted successfully!");
                // Refresh the list to remove the deleted plan
                loadUserNutritionPlans();
            }

            @Override
            public void onError(String error) {
                _error.setValue("Failed to delete nutrition plan: " + error);
            }
        });
    }

    public void clearCreatedPlan() {
        _createdPlan.setValue(null);
    }

    public void completeNutritionPlan(String userNutritionPlanId) {
        _isLoading.setValue(true);
        usersRepository.completeNutritionPlan(userNutritionPlanId, new UsersRepository.UsersCallback<kotlin.Unit>() {
            @Override
            public void onSuccess(kotlin.Unit result) {
                _isLoading.setValue(false);
                _successMessage.setValue("Nutrition plan completed successfully!");
                // Refresh the list to update the plan status
                loadUserNutritionPlans();
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _error.setValue("Failed to complete nutrition plan: " + error);
            }
        });
    }
}
