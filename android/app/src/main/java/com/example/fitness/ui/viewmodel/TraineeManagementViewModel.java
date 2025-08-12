package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.WorkoutPlanAssignment;
import com.example.fitness.data.network.model.generated.NutritionPlanAssignment;
import com.example.fitness.data.network.model.generated.WorkoutPlan;
import com.example.fitness.data.network.model.generated.NutritionPlan;
import com.example.fitness.data.network.model.generated.AssignWorkoutPlan;
import com.example.fitness.data.network.model.generated.AssignNutritionPlan;
import com.example.fitness.data.repository.AuthRepository;
import com.example.fitness.data.repository.UsersRepository;
import com.example.fitness.data.repository.WorkoutsRepository;
import com.example.fitness.data.repository.NutritionRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class TraineeManagementViewModel extends ViewModel {
    private final UsersRepository usersRepository;
    private final WorkoutsRepository workoutsRepository;
    private final NutritionRepository nutritionRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<List<WorkoutPlanAssignment>> _traineeWorkoutPlans = new MutableLiveData<>();
    public final LiveData<List<WorkoutPlanAssignment>> traineeWorkoutPlans = _traineeWorkoutPlans;

    private final MutableLiveData<List<NutritionPlanAssignment>> _traineeNutritionPlans = new MutableLiveData<>();
    public final LiveData<List<NutritionPlanAssignment>> traineeNutritionPlans = _traineeNutritionPlans;

    private final MutableLiveData<List<WorkoutPlan>> _availableWorkoutPlans = new MutableLiveData<>();
    public final LiveData<List<WorkoutPlan>> availableWorkoutPlans = _availableWorkoutPlans;

    private final MutableLiveData<List<NutritionPlan>> _availableNutritionPlans = new MutableLiveData<>();
    public final LiveData<List<NutritionPlan>> availableNutritionPlans = _availableNutritionPlans;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<String> _successMessage = new MutableLiveData<>();
    public final LiveData<String> successMessage = _successMessage;

    private String currentTraineeId;
    private String currentCoachId;
    private CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public TraineeManagementViewModel(
            UsersRepository usersRepository,
            WorkoutsRepository workoutsRepository,
            NutritionRepository nutritionRepository,
            AuthRepository authRepository) {
        this.usersRepository = usersRepository;
        this.workoutsRepository = workoutsRepository;
        this.nutritionRepository = nutritionRepository;
        this.authRepository = authRepository;
        
        // Subscribe to get current user ID
        disposables.add(
            authRepository.getCurrentUserId()
                .take(1) // Take only the first emission
                .subscribe(
                    userId -> this.currentCoachId = userId,
                    error -> this.currentCoachId = null
                )
        );
    }

    public void setTraineeId(String traineeId) {
        this.currentTraineeId = traineeId;
    }

    public void loadTraineeWorkoutPlans() {
        if (currentTraineeId == null) return;

        _isLoading.setValue(true);
        usersRepository.getUserWorkoutPlans(currentTraineeId, new UsersRepository.UsersCallback<List<WorkoutPlanAssignment>>() {
            @Override
            public void onSuccess(List<WorkoutPlanAssignment> result) {
                _isLoading.setValue(false);
                // Filter by coach id
                List<WorkoutPlanAssignment> filteredPlans = new ArrayList<>();
                for (WorkoutPlanAssignment assignment : result) {
                    if (currentCoachId.equals(assignment.getAssignedBy())) {
                        filteredPlans.add(assignment);
                    }
                }
                _traineeWorkoutPlans.setValue(filteredPlans);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void loadTraineeNutritionPlans() {
        if (currentTraineeId == null) return;

        _isLoading.setValue(true);
        usersRepository.getUserNutritionPlans(currentTraineeId, new UsersRepository.UsersCallback<List<NutritionPlanAssignment>>() {
            @Override
            public void onSuccess(List<NutritionPlanAssignment> result) {
                _isLoading.setValue(false);
                // Filter by coach id
                List<NutritionPlanAssignment> filteredPlans = new ArrayList<>();
                for (NutritionPlanAssignment assignment : result) {
                    if (currentCoachId.equals(assignment.getAssignedBy())) {
                        filteredPlans.add(assignment);
                    }
                }
                _traineeNutritionPlans.setValue(filteredPlans);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void loadAvailableWorkoutPlans() {
        _isLoading.setValue(true);
        workoutsRepository.getAllWorkoutPlans(new WorkoutsRepository.WorkoutsCallback<List<WorkoutPlan>>() {
            @Override
            public void onSuccess(List<WorkoutPlan> result) {
                _isLoading.setValue(false);
                // Filter by current coach (creator)
                List<WorkoutPlan> filteredPlans = new ArrayList<>();
                for (WorkoutPlan plan : result) {
                    if (currentCoachId.equals(plan.getCreatedBy())) {
                        filteredPlans.add(plan);
                    }
                }
                _availableWorkoutPlans.setValue(filteredPlans);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void loadAvailableNutritionPlans() {
        _isLoading.setValue(true);
        nutritionRepository.getAllNutritionPlans(new NutritionRepository.NutritionCallback<List<NutritionPlan>>() {
            @Override
            public void onSuccess(List<NutritionPlan> result) {
                _isLoading.setValue(false);
                // Filter by current coach (creator)
                List<NutritionPlan> filteredPlans = new ArrayList<>();
                for (NutritionPlan plan : result) {
                    if (currentCoachId.equals(plan.getCreatedBy())) {
                        filteredPlans.add(plan);
                    }
                }
                _availableNutritionPlans.setValue(filteredPlans);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void assignWorkoutPlan(int workoutPlanId) {
        if (currentTraineeId == null) return;

        _isLoading.setValue(true);
        AssignWorkoutPlan assignWorkoutPlan = new AssignWorkoutPlan(currentTraineeId, getCurrentDate());

        usersRepository.assignWorkoutPlan(String.valueOf(workoutPlanId), assignWorkoutPlan, 
            new UsersRepository.UsersCallback<com.example.fitness.data.network.model.generated.WorkoutPlanAssignmentResponse>() {
                @Override
                public void onSuccess(com.example.fitness.data.network.model.generated.WorkoutPlanAssignmentResponse result) {
                    _isLoading.setValue(false);
                    _successMessage.setValue("Workout plan assigned successfully");
                    loadTraineeWorkoutPlans(); // Refresh the list
                }

                @Override
                public void onError(String error) {
                    _isLoading.setValue(false);
                    _errorMessage.setValue(error);
                }
            });
    }

    public void assignNutritionPlan(int nutritionPlanId) {
        if (currentTraineeId == null) return;

        _isLoading.setValue(true);
        AssignNutritionPlan assignNutritionPlan = new AssignNutritionPlan(currentTraineeId, getCurrentDate());

        usersRepository.assignNutritionPlan(String.valueOf(nutritionPlanId), assignNutritionPlan, 
            new UsersRepository.UsersCallback<com.example.fitness.data.network.model.generated.NutritionPlanAssignmentResponse>() {
                @Override
                public void onSuccess(com.example.fitness.data.network.model.generated.NutritionPlanAssignmentResponse result) {
                    _isLoading.setValue(false);
                    _successMessage.setValue("Nutrition plan assigned successfully");
                    loadTraineeNutritionPlans(); // Refresh the list
                }

                @Override
                public void onError(String error) {
                    _isLoading.setValue(false);
                    _errorMessage.setValue(error);
                }
            });
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public void clearMessages() {
        _errorMessage.setValue(null);
        _successMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
