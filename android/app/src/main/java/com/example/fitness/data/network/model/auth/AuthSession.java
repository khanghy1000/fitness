package com.example.fitness.data.network.model.auth;

import com.squareup.moshi.Json;

public class AuthSession {
    @Json(name = "id")
    private String id;
    
    @Json(name = "expiresAt")
    private String expiresAt;
    
    @Json(name = "token")
    private String token;
    
    @Json(name = "createdAt")
    private String createdAt;
    
    @Json(name = "updatedAt")
    private String updatedAt;
    
    @Json(name = "ipAddress")
    private String ipAddress;
    
    @Json(name = "userAgent")
    private String userAgent;
    
    @Json(name = "userId")
    private String userId;

    public AuthSession() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
