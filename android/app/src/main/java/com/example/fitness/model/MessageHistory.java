package com.example.fitness.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Data;

/**
 * Holds messages of a conversation for easy binding.
 */
@Data
public class MessageHistory {
    private final List<Message> messages = new ArrayList<>();

    public void setMessages(List<Message> newMessages) {
        messages.clear();
        if (newMessages != null) {
            messages.addAll(newMessages);
        }
    }

    public void addMessage(Message message) {
        if (message != null) {
            messages.add(message);
        }
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }
}
