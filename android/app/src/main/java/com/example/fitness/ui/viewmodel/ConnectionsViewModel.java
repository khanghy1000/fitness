package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.ConnectionWithoutCoachTrainee;
import com.example.fitness.data.repository.AuthRepository;
import com.example.fitness.data.repository.ConnectionsRepository;
import com.example.fitness.data.network.model.generated.Connection;
import com.example.fitness.data.network.model.generated.ConnectRequest;
import com.example.fitness.data.network.model.generated.SuccessMessage;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ConnectionsViewModel extends ViewModel {
    private final ConnectionsRepository connectionsRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<List<Connection>> _activeConnections = new MutableLiveData<>();
    public final LiveData<List<Connection>> activeConnections = _activeConnections;

    private final MutableLiveData<List<Connection>> _sentRequests = new MutableLiveData<>();
    public final LiveData<List<Connection>> sentRequests = _sentRequests;

    private final MutableLiveData<List<Connection>> _receivedRequests = new MutableLiveData<>();
    public final LiveData<List<Connection>> receivedRequests = _receivedRequests;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<String> _successMessage = new MutableLiveData<>();
    public final LiveData<String> successMessage = _successMessage;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    @Inject
    public ConnectionsViewModel(ConnectionsRepository connectionsRepository, AuthRepository authRepository) {
        this.connectionsRepository = connectionsRepository;
        this.authRepository = authRepository;
    }

    public void loadActiveConnections() {
        _isLoading.setValue(true);
        connectionsRepository.getActiveConnections(new ConnectionsRepository.ConnectionsCallback<List<Connection>>() {
            @Override
            public void onSuccess(List<Connection> result) {
                _isLoading.setValue(false);
                _activeConnections.setValue(result);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void loadSentRequests() {
        _isLoading.setValue(true);
        connectionsRepository.getConnectionRequests("sent", new ConnectionsRepository.ConnectionsCallback<List<Connection>>() {
            @Override
            public void onSuccess(List<Connection> result) {
                _isLoading.setValue(false);
                _sentRequests.setValue(result);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void loadReceivedRequests() {
        _isLoading.setValue(true);
        connectionsRepository.getConnectionRequests("received", new ConnectionsRepository.ConnectionsCallback<List<Connection>>() {
            @Override
            public void onSuccess(List<Connection> result) {
                _isLoading.setValue(false);
                _receivedRequests.setValue(result);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void sendConnectionRequest(String coachId, String notes) {
        _isLoading.setValue(true);
        ConnectRequest request = new ConnectRequest(coachId, notes);
        connectionsRepository.sendConnectionRequest(request, new ConnectionsRepository.ConnectionsCallback<ConnectionWithoutCoachTrainee>() {
            @Override
            public void onSuccess(ConnectionWithoutCoachTrainee result) {
                _isLoading.setValue(false);
                _successMessage.setValue("Connection request sent successfully");
                loadSentRequests(); // Refresh sent requests
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void acceptConnectionRequest(String traineeId) {
        _isLoading.setValue(true);
        connectionsRepository.acceptConnectionRequest(traineeId, new ConnectionsRepository.ConnectionsCallback<ConnectionWithoutCoachTrainee>() {
            @Override
            public void onSuccess(ConnectionWithoutCoachTrainee result) {
                _isLoading.setValue(false);
                _successMessage.setValue("Connection request accepted");
                loadReceivedRequests(); // Refresh received requests
                loadActiveConnections(); // Refresh active connections
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void rejectConnectionRequest(String traineeId) {
        _isLoading.setValue(true);
        connectionsRepository.rejectConnectionRequest(traineeId, new ConnectionsRepository.ConnectionsCallback<SuccessMessage>() {
            @Override
            public void onSuccess(SuccessMessage result) {
                _isLoading.setValue(false);
                _successMessage.setValue("Connection request rejected");
                loadReceivedRequests(); // Refresh received requests
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void disconnectTrainee(String traineeId) {
        _isLoading.setValue(true);
        connectionsRepository.disconnectTrainee(traineeId, new ConnectionsRepository.ConnectionsCallback<SuccessMessage>() {
            @Override
            public void onSuccess(SuccessMessage result) {
                _isLoading.setValue(false);
                _successMessage.setValue("Trainee disconnected successfully");
                loadActiveConnections(); // Refresh active connections
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
        _successMessage.setValue(null);
    }
}
