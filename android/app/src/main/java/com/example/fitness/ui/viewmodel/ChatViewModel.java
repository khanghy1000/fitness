package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.SocketService;
import com.example.fitness.data.local.AuthDataStore;
import com.example.fitness.model.Message;
import com.example.fitness.model.ConversationSummary;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel dedicated to a single chat thread with a specific user.
 */
@HiltViewModel
public class ChatViewModel extends ViewModel implements SocketService.SocketEventListener {
    private final SocketService socketService;

    private final MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> connectionStatus = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> typingRemote = new MutableLiveData<>(false);

    private String remoteUserId; // The user we are chatting with
    private String currentUserId; // Cached current user id for read status updates

    @Inject
    public ChatViewModel(SocketService socketService, AuthDataStore authDataStore) {
        this.socketService = socketService;
    this.socketService.addEventListener(this);
        // Async fetch current user id
        authDataStore.getUserIdSync()
                .subscribe(id -> currentUserId = id, err -> { /* ignore */ });
    }

    public void init(String remoteUserId) {
        this.remoteUserId = remoteUserId;
    }

    public LiveData<List<Message>> getMessages() { return messagesLiveData; }
    public LiveData<Boolean> getConnectionStatus() { return connectionStatus; }
    public LiveData<String> getErrors() { return errorLiveData; }
    public LiveData<Boolean> getTypingRemote() { return typingRemote; }

    public void connect() { if (!socketService.isConnected()) socketService.connect(); }
    public void disconnect() { socketService.disconnect(); }
    public boolean isConnected() { return socketService.isConnected(); }

    public void loadConversation(int limit, int offset) {
        if (remoteUserId != null) {
            socketService.getConversation(remoteUserId, limit, offset);
        }
    }

    public void sendMessage(String content) { if (remoteUserId != null && content != null && !content.trim().isEmpty()) socketService.sendMessage(remoteUserId, content.trim(), null); }
    public void startTyping() { if (remoteUserId != null) socketService.startTyping(remoteUserId); }
    public void stopTyping() { if (remoteUserId != null) socketService.stopTyping(remoteUserId); }
    public void markMessagesRead() { if (remoteUserId != null) socketService.markMessagesAsRead(remoteUserId); }

    // Socket callbacks
    @Override
    public void onNewMessage(Message message) {
        if (message == null) return;
        if (remoteUserId != null && (remoteUserId.equals(message.getSenderId()) || remoteUserId.equals(message.getRecipientId()))) {
            List<Message> current = messagesLiveData.getValue();
            if (current == null) current = new ArrayList<>();
            current = new ArrayList<>(current); // copy
            current.add(message);
            messagesLiveData.postValue(current);
            // Immediately mark as read if it's an incoming message to me
            if (remoteUserId.equals(message.getSenderId())) {
                markMessagesRead();
            }
        }
    }

    @Override
    public void onMessageSent(Message message) { onNewMessage(message); }

    @Override
    public void onConversationHistory(String userId, List<Message> messages) {
        if (remoteUserId != null && remoteUserId.equals(userId)) {
            messagesLiveData.postValue(messages != null ? new ArrayList<>(messages) : new ArrayList<>());
            // After loading history, mark any incoming unread messages as read
            markMessagesRead();
        }
    }

    @Override
    public void onConversationsList(List<ConversationSummary> conversations) { /* Not used here */ }
    @Override
    public void onConnected() { connectionStatus.postValue(true); }
    @Override
    public void onUnreadCount(int count) { }
    @Override
    public void onMessagesRead(String readBy, String readAt) {
        // readBy is the user who read our messages. If it's the remoteUser, update local outgoing messages
        if (remoteUserId == null || currentUserId == null) return;
        if (!remoteUserId.equals(readBy)) return; // not our active chat
        List<Message> current = messagesLiveData.getValue();
        if (current == null || current.isEmpty()) return;
        boolean changed = false;
        List<Message> updated = new ArrayList<>(current.size());
        for (Message m : current) {
            if (m.getSenderId().equals(currentUserId) && m.getRecipientId().equals(remoteUserId) && !m.isRead()) {
                m.setRead(true);
                if (m.getReadAt() == null) m.setReadAt(readAt);
                changed = true;
            }
            updated.add(m);
        }
        if (changed) messagesLiveData.postValue(updated);
    }
    @Override
    public void onUserTyping(String userId, String userName) { if (remoteUserId != null && remoteUserId.equals(userId)) typingRemote.postValue(true); }
    @Override
    public void onUserStoppedTyping(String userId) { if (remoteUserId != null && remoteUserId.equals(userId)) typingRemote.postValue(false); }
    @Override
    public void onUserStatusChanged(String userId, String status) { }
    @Override
    public void onError(String error) { errorLiveData.postValue(error); }
    @Override
    public void onDisconnected() { connectionStatus.postValue(false); }

    @Override
    protected void onCleared() {
        super.onCleared();
    socketService.removeEventListener(this);
    }
}
