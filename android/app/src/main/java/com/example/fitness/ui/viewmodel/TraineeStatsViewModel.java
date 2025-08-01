package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.repository.UsersRepository;
import com.example.fitness.data.network.model.generated.RecordUserStats;
import com.example.fitness.data.network.model.generated.UserStats;
import com.example.fitness.data.network.model.generated.UserStatsResponse;
import com.example.fitness.data.network.model.generated.LatestUserStats;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TraineeStatsViewModel extends ViewModel {
    
    private final UsersRepository usersRepository;
    
    // LiveData for UI state
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;
    
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;
    
    private final MutableLiveData<Boolean> _recordSuccess = new MutableLiveData<>();
    public final LiveData<Boolean> recordSuccess = _recordSuccess;
    
    private final MutableLiveData<List<UserStats>> _userStats = new MutableLiveData<>();
    public final LiveData<List<UserStats>> userStats = _userStats;
    
    private final MutableLiveData<LatestUserStats> _latestStats = new MutableLiveData<>();
    public final LiveData<LatestUserStats> latestStats = _latestStats;

    @Inject
    public TraineeStatsViewModel(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public void loadUserStats() {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        
        usersRepository.getUserStats(new UsersRepository.UsersCallback<List<UserStats>>() {
            @Override
            public void onSuccess(List<UserStats> result) {
                _isLoading.setValue(false);
                _userStats.setValue(result);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void loadLatestStats() {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        
        usersRepository.getLatestUserStats(new UsersRepository.UsersCallback<LatestUserStats>() {
            @Override
            public void onSuccess(LatestUserStats result) {
                _isLoading.setValue(false);
                _latestStats.setValue(result);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void recordStats(Double weight, Double height, Double bodyFat, Double muscleMass,
                           Double chest, Double waist, Double hips, Double arms, 
                           Double thighs, String notes) {
        
        // Validate input
        if (weight == null && height == null && bodyFat == null && muscleMass == null &&
            chest == null && waist == null && hips == null && arms == null && thighs == null) {
            _errorMessage.setValue("Please enter at least one measurement");
            return;
        }
        
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        
        RecordUserStats recordUserStats = new RecordUserStats(
            weight != null ? BigDecimal.valueOf(weight) : null,
            height != null ? BigDecimal.valueOf(height) : null,
            bodyFat != null ? BigDecimal.valueOf(bodyFat) : null,
            muscleMass != null ? BigDecimal.valueOf(muscleMass) : null,
            chest != null ? BigDecimal.valueOf(chest) : null,
            waist != null ? BigDecimal.valueOf(waist) : null,
            hips != null ? BigDecimal.valueOf(hips) : null,
            arms != null ? BigDecimal.valueOf(arms) : null,
            thighs != null ? BigDecimal.valueOf(thighs) : null,
            notes
        );
        
        usersRepository.recordUserStats(recordUserStats, new UsersRepository.UsersCallback<UserStatsResponse>() {
            @Override
            public void onSuccess(UserStatsResponse result) {
                _isLoading.setValue(false);
                _recordSuccess.setValue(true);
                // Reload stats after successful recording
                loadUserStats();
                loadLatestStats();
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void clearMessages() {
        _errorMessage.setValue(null);
        _recordSuccess.setValue(false);
    }
}
