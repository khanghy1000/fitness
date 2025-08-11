package com.example.fitness.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.squareup.moshi.Json;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationSummary {
    @Json(name = "userId")
    private String userId;
    @Json(name = "userName")
    private String userName;
    @Json(name = "lastMessage")
    private String lastMessage;
    @Json(name = "lastMessageAt")
    private String lastMessageAt;
    @Json(name = "unreadCount")
    private int unreadCount;
}
