package com.example.fitness.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MessageHistory {
    private String senderName;
    private String messageContent;
    private String timestamp;
    private boolean isRead;
}
