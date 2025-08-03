package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.DetailedNutritionPlan;
import com.example.fitness.data.network.model.generated.NutritionPlanDay;
import com.example.fitness.data.repository.NutritionRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NutritionPlanDetailsViewModel extends ViewModel {
    private final NutritionRepository nutritionRepository;

    private final MutableLiveData<DetailedNutritionPlan> _detailedNutritionPlan = new MutableLiveData<>();
    public final LiveData<DetailedNutritionPlan> detailedNutritionPlan = _detailedNutritionPlan;

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
        
        // Load nutrition plan details (includes days)
        nutritionRepository.getNutritionPlanById(planId, new NutritionRepository.NutritionCallback<DetailedNutritionPlan>() {
            @Override
            public void onSuccess(DetailedNutritionPlan result) {
                _detailedNutritionPlan.setValue(result);
                _error.setValue(null);
                
                // Extract days from the detailed nutrition plan
                if (result.getDays() != null) {
                    // Convert DetailedNutritionPlanDay to NutritionPlanDay
                    List<NutritionPlanDay> nutritionPlanDays = new ArrayList<>();
                    for (com.example.fitness.data.network.model.generated.DetailedNutritionPlanDay detailedDay : result.getDays()) {
                        // Create a basic NutritionPlanDay from DetailedNutritionPlanDay
                        // Note: You might need to adjust this based on actual NutritionPlanDay structure
                        nutritionPlanDays.add(convertToNutritionPlanDay(detailedDay));
                    }
                    _nutritionPlanDays.setValue(nutritionPlanDays);
                }
                
                _isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _error.setValue(error);
            }
        });
    }

    private NutritionPlanDay convertToNutritionPlanDay(com.example.fitness.data.network.model.generated.DetailedNutritionPlanDay detailedDay) {
        // Convert DetailedNutritionPlanDay to NutritionPlanDay
        // Convert the weekday enum from DetailedNutritionPlanDay.Weekday to NutritionPlanDay.Weekday
        NutritionPlanDay.Weekday weekday = NutritionPlanDay.Weekday.valueOf(detailedDay.getWeekday().getValue());
        
        return new NutritionPlanDay(
            detailedDay.getId(),
            detailedDay.getNutritionPlanId(),
            weekday,
            detailedDay.getTotalCalories(),
            detailedDay.getProtein(),
            detailedDay.getCarbs(),
            detailedDay.getFat(),
            detailedDay.getFiber()
        );
    }

    public void refreshPlanDetails(String planId) {
        loadNutritionPlanDetails(planId);
    }

    public void clearError() {
        _error.setValue(null);
    }
}
