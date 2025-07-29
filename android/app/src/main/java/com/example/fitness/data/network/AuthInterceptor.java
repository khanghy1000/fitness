package com.example.fitness.data.network;

import com.example.fitness.data.local.AuthDataStore;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
public class AuthInterceptor implements Interceptor {
    private final AuthDataStore authDataStore;

    @Inject
    public AuthInterceptor(AuthDataStore authDataStore) {
        this.authDataStore = authDataStore;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Skip adding token for auth endpoints
        String url = originalRequest.url().toString();
        if (url.contains("/auth/sign-up") || url.contains("/auth/sign-in")) {
            return chain.proceed(originalRequest);
        }

        String token = authDataStore.getJwtToken();
        
        if (token != null && !token.isEmpty()) {
            Request authenticatedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(authenticatedRequest);
        }

        return chain.proceed(originalRequest);
    }
}
