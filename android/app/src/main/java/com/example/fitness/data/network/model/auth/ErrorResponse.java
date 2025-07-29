package com.example.fitness.data.network.model.auth;

import com.squareup.moshi.Json;

public class ErrorResponse {
    @Json(name = "message")
    private String message;

    public ErrorResponse() {}

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
