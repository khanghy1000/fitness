package com.example.fitness.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Message {
    private Long id;
    private String senderId;
    private String recipientId;
    private String content;
    private LocalDateTime readAt;
    private Boolean isRead;
    private Long replyToId;
    private LocalDateTime createdAt;
}
