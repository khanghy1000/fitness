package com.example.fitness.data.network.model;

import com.squareup.moshi.Json;

public class ErrorResponse {
    @Json(name = "message")
    private String message;
    
    @Json(name = "error")
    private String error;

    public ErrorResponse() {}

    public ErrorResponse(String message, String error) {
        this.message = message;
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * Get the error message, preferring 'error' field over 'message' field
     * @return The error message
     */
    public String getErrorMessage() {
        if (error != null && !error.trim().isEmpty()) {
            return error;
        }
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }
        return null;
    }
}
