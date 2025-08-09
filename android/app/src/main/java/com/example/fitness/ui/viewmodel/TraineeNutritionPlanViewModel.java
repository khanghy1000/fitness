package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.DetailedNutritionPlan;
import com.example.fitness.data.network.model.generated.MealCompletion;
import com.example.fitness.data.network.model.generated.MealCompletionResponse;
import com.example.fitness.data.network.model.generated.NutritionPlanAssignment;
import com.example.fitness.data.network.model.generated.WorkoutPlanAssignment;
import com.example.fitness.data.repository.NutritionRepository;
import com.example.fitness.data.repository.UsersRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TraineeNutritionPlanViewModel extends ViewModel {
    private final UsersRepository usersRepository;
    private final NutritionRepository nutritionRepository;

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

    @Inject
    public TraineeNutritionPlanViewModel(UsersRepository usersRepository, NutritionRepository nutritionRepository) {
        this.usersRepository = usersRepository;
        this.nutritionRepository = nutritionRepository;
        loadUserNutritionPlans();
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
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _isRefreshing.setValue(false);
                _error.setValue(error);
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
}
