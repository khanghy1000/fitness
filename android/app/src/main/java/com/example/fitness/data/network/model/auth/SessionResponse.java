package com.example.fitness.data.network.model.auth;

import com.squareup.moshi.Json;

public class SessionResponse {
    @Json(name = "session")
    private AuthSession session;
    
    @Json(name = "user")
    private AuthUser user;

    public SessionResponse() {}

    public SessionResponse(AuthSession session, AuthUser user) {
        this.session = session;
        this.user = user;
    }

    public AuthSession getSession() {
        return session;
    }

    public void setSession(AuthSession session) {
        this.session = session;
    }

    public AuthUser getUser() {
        return user;
    }

    public void setUser(AuthUser user) {
        this.user = user;
    }
}
