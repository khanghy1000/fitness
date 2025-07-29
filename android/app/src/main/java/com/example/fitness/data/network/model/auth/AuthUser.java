package com.example.fitness.data.network.model.auth;

import com.squareup.moshi.Json;

public class AuthUser {
    @Json(name = "id")
    private String id;
    
    @Json(name = "email")
    private String email;
    
    @Json(name = "name")
    private String name;
    
    @Json(name = "role")
    private String role; // Only present in session response

    public AuthUser() {}

    public AuthUser(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public AuthUser(String id, String email, String name, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
