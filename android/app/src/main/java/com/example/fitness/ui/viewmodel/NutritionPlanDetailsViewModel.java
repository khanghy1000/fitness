package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.NutritionPlan;
import com.example.fitness.data.network.model.generated.NutritionPlanDay;
import com.example.fitness.data.repository.NutritionRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NutritionPlanDetailsViewModel extends ViewModel {
    private final NutritionRepository nutritionRepository;

    private final MutableLiveData<NutritionPlan> _nutritionPlan = new MutableLiveData<>();
    public final LiveData<NutritionPlan> nutritionPlan = _nutritionPlan;

    private final MutableLiveData<List<NutritionPlanDay>> _nutritionPlanDays = new MutableLiveData<>();
    public final LiveData<List<NutritionPlanDay>> nutritionPlanDays = _nutritionPlanDays;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    @Inject
    public NutritionPlanDetailsViewModel(NutritionRepository nutritionRepository) {
        this.nutritionRepository = nutritionRepository;
    }

    public void loadNutritionPlanDetails(String planId) {
        _isLoading.setValue(true);
        
        // Load nutrition plan details
        nutritionRepository.getNutritionPlanById(planId, new NutritionRepository.NutritionCallback<NutritionPlan>() {
            @Override
            public void onSuccess(NutritionPlan result) {
                _nutritionPlan.setValue(result);
                _error.setValue(null);
                
                // Load nutrition plan days
                loadNutritionPlanDays(planId);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _error.setValue(error);
            }
        });
    }

    private void loadNutritionPlanDays(String planId) {
        nutritionRepository.getNutritionPlanDays(planId, new NutritionRepository.NutritionCallback<List<NutritionPlanDay>>() {
            @Override
            public void onSuccess(List<NutritionPlanDay> result) {
                _isLoading.setValue(false);
                _nutritionPlanDays.setValue(result);
                _error.setValue(null);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _error.setValue(error);
            }
        });
    }

    public void refreshPlanDetails(String planId) {
        loadNutritionPlanDetails(planId);
    }

    public void clearError() {
        _error.setValue(null);
    }
}
