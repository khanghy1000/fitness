package com.example.fitness.data.network.model.auth;

import com.squareup.moshi.Json;

public class SignInRequest {
    @Json(name = "email")
    private String email;
    
    @Json(name = "password")
    private String password;

    public SignInRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
