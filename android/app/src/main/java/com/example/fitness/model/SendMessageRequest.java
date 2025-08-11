package com.example.fitness.model;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SendMessageRequest {
    @Json(name = "recipientId")
    private String recipientId;

    @Json(name = "content")
    private String content;

    @Json(name = "replyToId")
    private String replyToId; // Optional - can be null
}