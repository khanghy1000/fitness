package com.example.fitness.model;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ConversationRequest {
    @Json(name = "userId")
    private String userId;

    @Json(name = "limit")
    private Integer limit; // Optional

    @Json(name = "offset")
    private Integer offset; // Optional
}