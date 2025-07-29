package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.repository.AuthRepository;
import com.example.fitness.data.network.model.auth.SignInResponse;
import com.example.fitness.data.network.model.auth.SignUpResponse;
import com.example.fitness.data.network.model.auth.SessionResponse;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {
    
    private final AuthRepository authRepository;
    
    // LiveData for UI state
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;
    
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;
    
    private final MutableLiveData<Boolean> _loginSuccess = new MutableLiveData<>();
    public final LiveData<Boolean> loginSuccess = _loginSuccess;
    
    private final MutableLiveData<Boolean> _registerSuccess = new MutableLiveData<>();
    public final LiveData<Boolean> registerSuccess = _registerSuccess;

    @Inject
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void signIn(String email, String password) {
        if (email.trim().isEmpty()) {
            _errorMessage.setValue("Email is required");
            return;
        }
        
        if (password.trim().isEmpty()) {
            _errorMessage.setValue("Password is required");
            return;
        }
        
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        
        authRepository.signIn(email, password, new AuthRepository.AuthCallback<SignInResponse>() {
            @Override
            public void onSuccess(SignInResponse result) {
                // After successful login, get the session to ensure we have the latest user data
                getSessionAfterAuth();
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void signUp(String name, String email, String password, String role) {
        if (name.trim().isEmpty()) {
            _errorMessage.setValue("Name is required");
            return;
        }
        
        if (email.trim().isEmpty()) {
            _errorMessage.setValue("Email is required");
            return;
        }
        
        if (password.trim().isEmpty()) {
            _errorMessage.setValue("Password is required");
            return;
        }
        
        if (password.length() < 6) {
            _errorMessage.setValue("Password must be at least 6 characters");
            return;
        }
        
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        
        authRepository.signUp(name, email, password, role, new AuthRepository.AuthCallback<SignUpResponse>() {
            @Override
            public void onSuccess(SignUpResponse result) {
                // After successful registration, get the session to ensure we have the latest user data
                getSessionAfterAuth();
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
            }
        });
    }

    public void clearError() {
        _errorMessage.setValue(null);
    }

    public void resetLoginSuccess() {
        _loginSuccess.setValue(false);
    }

    public void resetRegisterSuccess() {
        _registerSuccess.setValue(false);
    }

    /**
     * Get session data after successful authentication
     * This ensures we have the latest user information including role
     */
    private void getSessionAfterAuth() {
        authRepository.getSession(new AuthRepository.AuthCallback<SessionResponse>() {
            @Override
            public void onSuccess(SessionResponse result) {
                _isLoading.setValue(false);
                // Set both login and register success to true since session fetch succeeded
                // The activities will check for either one
                _loginSuccess.setValue(true);
                _registerSuccess.setValue(true);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Authentication succeeded but failed to get session: " + error);
            }
        });
    }
}
