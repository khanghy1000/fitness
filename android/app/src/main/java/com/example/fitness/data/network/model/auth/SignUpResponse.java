package com.example.fitness.data.network.model.auth;

import com.squareup.moshi.Json;

public class SignUpResponse {
    @Json(name = "token")
    private String token;
    
    @Json(name = "user")
    private AuthUser user;

    public SignUpResponse() {}

    public SignUpResponse(String token, AuthUser user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AuthUser getUser() {
        return user;
    }

    public void setUser(AuthUser user) {
        this.user = user;
    }
}
