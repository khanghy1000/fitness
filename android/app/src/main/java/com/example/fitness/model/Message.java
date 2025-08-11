package com.example.fitness.model;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import com.squareup.moshi.Json;

@Getter
@Setter
@AllArgsConstructor
public class Message {
    @Json(name = "id")
    private int id;

    @Json(name = "senderId")
    private String senderId;

    @Json(name = "recipientId")
    private String recipientId;

    @Json(name = "content")
    private String content;

    @Json(name = "readAt")
    private String readAt;

    @Json(name = "isRead")
    private boolean isRead;

    @Json(name = "replyToId")
    private String replyToId;

    @Json(name = "createdAt")
    private String createdAt;

    public Message(String senderId, String recipientId, String content) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.isRead = false;
        // Generate ISO 8601 timestamp
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        this.createdAt = isoFormat.format(new Date());
        this.readAt = null; // Initially not read
        this.replyToId = null; // Initially no reply
        this.id = 0; // ID will be set by the server or database
    }
}
