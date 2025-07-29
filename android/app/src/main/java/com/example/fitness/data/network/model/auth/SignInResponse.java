package com.example.fitness.data.network.model.auth;

import com.squareup.moshi.Json;

public class SignInResponse {
    @Json(name = "redirect")
    private boolean redirect;
    
    @Json(name = "token")
    private String token;
    
    @Json(name = "url")
    private String url;
    
    @Json(name = "user")
    private AuthUser user;

    public SignInResponse() {}

    public SignInResponse(boolean redirect, String token, String url, AuthUser user) {
        this.redirect = redirect;
        this.token = token;
        this.url = url;
        this.user = user;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public AuthUser getUser() {
        return user;
    }

    public void setUser(AuthUser user) {
        this.user = user;
    }
}
