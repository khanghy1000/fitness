package com.example.fitness.data.network.model.auth;

import com.squareup.moshi.Json;

public class SignUpRequest {
    @Json(name = "name")
    private String name;
    
    @Json(name = "email")
    private String email;
    
    @Json(name = "password")
    private String password;
    
    @Json(name = "role")
    private String role; // "coach" or "trainee"

    public SignUpRequest(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
