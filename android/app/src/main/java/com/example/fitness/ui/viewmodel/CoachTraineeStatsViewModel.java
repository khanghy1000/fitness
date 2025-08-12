package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.repository.UsersRepository;
import com.example.fitness.data.network.model.generated.UserStats;
import com.example.fitness.data.network.model.generated.LatestUserStats;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CoachTraineeStatsViewModel extends ViewModel {
    
    private final UsersRepository usersRepository;
    
    // LiveData for UI state
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;
    
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;
    
    private final MutableLiveData<List<UserStats>> _userStats = new MutableLiveData<>();
    public final LiveData<List<UserStats>> userStats = _userStats;
    
    private final MutableLiveData<LatestUserStats> _latestStats = new MutableLiveData<>();
    public final LiveData<LatestUserStats> latestStats = _latestStats;

    private String traineeId;

    @Inject
    public CoachTraineeStatsViewModel(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public void setTraineeId(String traineeId) {
        this.traineeId = traineeId;
        if (traineeId != null) {
            loadTraineeStats();
            loadTraineeLatestStats();
        }
    }

    public void loadTraineeStats() {
        if (traineeId == null) return;
        
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        
        usersRepository.getUserStats(traineeId, new UsersRepository.UsersCallback<List<UserStats>>() {
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

    public void loadTraineeLatestStats() {
        if (traineeId == null) return;
        
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        
        usersRepository.getLatestUserStats(traineeId, new UsersRepository.UsersCallback<LatestUserStats>() {
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

    public void clearMessages() {
        _errorMessage.setValue(null);
    }
}
