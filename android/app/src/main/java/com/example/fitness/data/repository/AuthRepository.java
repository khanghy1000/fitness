package com.example.fitness.data.repository;

import com.example.fitness.data.local.AuthDataStore;
import com.example.fitness.data.network.retrofit.AuthApi;
import com.example.fitness.data.network.model.auth.SessionResponse;
import com.example.fitness.data.network.model.auth.SignInRequest;
import com.example.fitness.data.network.model.auth.SignInResponse;
import com.example.fitness.data.network.model.auth.SignUpRequest;
import com.example.fitness.data.network.model.auth.SignUpResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class AuthRepository {
    private final AuthApi authApi;
    private final AuthDataStore authDataStore;

    @Inject
    public AuthRepository(AuthApi authApi, AuthDataStore authDataStore) {
        this.authApi = authApi;
        this.authDataStore = authDataStore;
    }

    public interface AuthCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void signUp(String name, String email, String password, String role, AuthCallback<SignUpResponse> callback) {
        SignUpRequest request = new SignUpRequest(name, email, password, role);
        authApi.signUp(request).enqueue(new Callback<SignUpResponse>() {
            @Override
            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SignUpResponse signUpResponse = response.body();
                    // Save auth data to DataStore
                    authDataStore.saveAuthData(
                            signUpResponse.getToken(),
                            signUpResponse.getUser().getId(),
                            signUpResponse.getUser().getEmail(),
                            signUpResponse.getUser().getName(),
                            signUpResponse.getUser().getRole()
                    );
                    callback.onSuccess(signUpResponse);
                } else {
                    callback.onError("Sign up failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SignUpResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void signIn(String email, String password, AuthCallback<SignInResponse> callback) {
        SignInRequest request = new SignInRequest(email, password);
        authApi.signIn(request).enqueue(new Callback<SignInResponse>() {
            @Override
            public void onResponse(Call<SignInResponse> call, Response<SignInResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SignInResponse signInResponse = response.body();
                    // Save auth data to DataStore
                    authDataStore.saveAuthData(
                            signInResponse.getToken(),
                            signInResponse.getUser().getId(),
                            signInResponse.getUser().getEmail(),
                            signInResponse.getUser().getName(),
                            signInResponse.getUser().getRole()
                    );
                    callback.onSuccess(signInResponse);
                } else {
                    callback.onError("Sign in failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SignInResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getSession(AuthCallback<SessionResponse> callback) {
        authApi.getSession().enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SessionResponse sessionResponse = response.body();
                    // Update user data in DataStore
                    authDataStore.saveAuthData(
                            sessionResponse.getSession().getToken(),
                            sessionResponse.getUser().getId(),
                            sessionResponse.getUser().getEmail(),
                            sessionResponse.getUser().getName(),
                            sessionResponse.getUser().getRole()
                    );
                    callback.onSuccess(sessionResponse);
                } else {
                    callback.onError("Get session failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SessionResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void logout() {
        authDataStore.clearAuthData();
    }

    public boolean isLoggedIn() {
        return authDataStore.isLoggedIn();
    }

    public String getCurrentUserId() {
        return authDataStore.getUserId();
    }

    public String getCurrentUserEmail() {
        return authDataStore.getUserEmail();
    }

    public String getCurrentUserName() {
        return authDataStore.getUserName();
    }

    public String getCurrentUserRole() {
        return authDataStore.getUserRole();
    }

    public String getJwtToken() {
        return authDataStore.getJwtToken();
    }
}
