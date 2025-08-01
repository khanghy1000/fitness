package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class MainViewModel extends ViewModel {
    
    private final AuthRepository authRepository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    
    private final MutableLiveData<String> _welcomeMessage = new MutableLiveData<>();
    public final LiveData<String> welcomeMessage = _welcomeMessage;
    
    private final MutableLiveData<String> _userInfo = new MutableLiveData<>();
    public final LiveData<String> userInfo = _userInfo;
    
    private final MutableLiveData<String> _userRole = new MutableLiveData<>();
    public final LiveData<String> userRole = _userRole;

    @Inject
    public MainViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
        loadUserData();
    }

    private void loadUserData() {
        // Load user name for welcome message
        compositeDisposable.add(
            authRepository.getCurrentUserName()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    name -> {
                        _welcomeMessage.setValue("Welcome, " + name + "!");
                    },
                    throwable -> _welcomeMessage.setValue("Welcome!")
                )
        );

        // Load user email and role for user info
        compositeDisposable.add(
            authRepository.getCurrentUserEmail()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    email -> {
                        // Load role after getting email
                        compositeDisposable.add(
                            authRepository.getCurrentUserRole()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    role -> {
                                        String userInfo = "Email: " + email + "\nRole: " + role.toUpperCase();
                                        _userInfo.setValue(userInfo);
                                        _userRole.setValue(role);
                                    },
                                    throwable -> {
                                        _userInfo.setValue("Email: " + email);
                                        _userRole.setValue(null);
                                    }
                                )
                        );
                    },
                    throwable -> {
                        _userInfo.setValue("User information not available");
                        _userRole.setValue(null);
                    }
                )
        );
    }

    public void logout() {
        authRepository.logout();
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
