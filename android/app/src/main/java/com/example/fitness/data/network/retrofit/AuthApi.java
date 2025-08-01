package com.example.fitness.data.network.retrofit;

import retrofit2.Call;
import retrofit2.http.*;

import com.example.fitness.data.network.model.auth.SignUpRequest;
import com.example.fitness.data.network.model.auth.SignUpResponse;
import com.example.fitness.data.network.model.auth.SignInRequest;
import com.example.fitness.data.network.model.auth.SignInResponse;
import com.example.fitness.data.network.model.auth.SessionResponse;

public interface AuthApi {
    /**
     * Sign up with email
     * Create a new user account
     * @param signUpRequest Sign up data (required)
     * @return Call&lt;SignUpResponse&gt;
     */
    @Headers({
        "Content-Type:application/json"
    })
    @POST("/api/auth/sign-up/email")
    Call<SignUpResponse> signUp(
        @Body SignUpRequest signUpRequest
    );

    /**
     * Sign in with email
     * Authenticate user with email and password
     * @param signInRequest Sign in data (required)
     * @return Call&lt;SignInResponse&gt;
     */
    @Headers({
        "Content-Type:application/json"
    })
    @POST("/api/auth/sign-in/email")
    Call<SignInResponse> signIn(
        @Body SignInRequest signInRequest
    );

    /**
     * Get current session
     * Retrieve current user session (requires JWT token)
     * @return Call&lt;SessionResponse&gt;
     */
    @GET("/api/auth/get-session")
    Call<SessionResponse> getSession();
}
