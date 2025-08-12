package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.SocketService;
import com.example.fitness.data.local.AuthDataStore;
import com.example.fitness.model.ConversationSummary;
import com.example.fitness.model.Message;
import com.example.fitness.model.MessageHistory;
import com.example.fitness.data.network.model.generated.Connection;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MessageHistoryViewModel extends ViewModel implements SocketService.SocketEventListener {
    private final SocketService socketService;

    private final MutableLiveData<MessageHistory> historyLiveData = new MutableLiveData<>(new MessageHistory());
    private final MutableLiveData<Boolean> connectionStatus = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ConversationSummary>> conversationsLiveData = new MutableLiveData<>();

    private final AuthDataStore authDataStore;
    private String currentUserId; // cached current user id
    private List<Connection> activeConnections; // cached active connections for user name lookup

    @Inject
    public MessageHistoryViewModel(SocketService socketService, AuthDataStore authDataStore) {
        this.socketService = socketService;
        this.authDataStore = authDataStore;
    this.socketService.addEventListener(this);
        // Fetch current user id (fire & forget)
        authDataStore.getUserIdSync()
                .subscribe(id -> currentUserId = id, err -> { /* ignore */ });
    }

    public LiveData<MessageHistory> getMessageHistory() { return historyLiveData; }
    public LiveData<Boolean> getConnectionStatus() { return connectionStatus; }
    public LiveData<String> getErrors() { return errorLiveData; }
    public LiveData<List<ConversationSummary>> getConversations() { return conversationsLiveData; }

    // Control methods
    public void connect() { if (!socketService.isConnected()) socketService.connect(); }
    public void disconnect() { socketService.disconnect(); }
    public boolean isConnected() { return socketService.isConnected(); }

    public void loadConversation(String userId, int limit, int offset) { socketService.getConversation(userId, limit, offset); }
    public void sendMessage(String recipientId, String content, String replyToId) { socketService.sendMessage(recipientId, content, replyToId); }
    public void markMessagesAsRead(String conversationUserId) {
        socketService.markMessagesAsRead(conversationUserId);
        List<ConversationSummary> current = conversationsLiveData.getValue();
        if (current != null && conversationUserId != null) {
            boolean changed = false;
            java.util.ArrayList<ConversationSummary> updated = new java.util.ArrayList<>(current.size());
            for (ConversationSummary cs : current) {
                if (cs.getUserId().equals(conversationUserId) && cs.getUnreadCount() > 0) {
                    ConversationSummary newCs = new ConversationSummary(cs.getUserId(), cs.getUserName(), cs.getLastMessage(), cs.getLastMessageAt(), 0);
                    updated.add(newCs);
                    changed = true;
                } else {
                    updated.add(cs);
                }
            }
            if (changed) conversationsLiveData.postValue(updated);
        }
    }
    public void startTyping(String recipientId) { socketService.startTyping(recipientId); }
    public void stopTyping(String recipientId) { socketService.stopTyping(recipientId); }
    public void setUserOnline() { socketService.setUserOnline(); }
    public void requestUnreadCount() { socketService.getUnreadCount(); }
    public void requestConversationsList() { socketService.getConversationsList(); }

    // Method to set active connections for user name lookup
    public void setActiveConnections(List<Connection> connections) {
        this.activeConnections = connections;
    }

    // Helper method to get user name by user ID from active connections
    private String getUserNameById(String userId) {
        if (activeConnections == null || userId == null || currentUserId == null) {
            return null;
        }
        
        for (Connection connection : activeConnections) {
            try {
                if (currentUserId.equals(connection.getCoachId()) && userId.equals(connection.getTraineeId())) {
                    // Current user is coach, looking for trainee name
                    return connection.getTrainee() != null ? connection.getTrainee().getName() : null;
                } else if (currentUserId.equals(connection.getTraineeId()) && userId.equals(connection.getCoachId())) {
                    // Current user is trainee, looking for coach name
                    return connection.getCoach() != null ? connection.getCoach().getName() : null;
                }
            } catch (Exception ignored) {
                // Ignore any errors accessing connection properties
            }
        }
        return null;
    }

    // Socket callbacks
    @Override
    public void onNewMessage(Message message) {
        // Update detailed message history if currently viewing a conversation (not the focus here)
        MessageHistory current = historyLiveData.getValue();
        if (current == null) current = new MessageHistory();
        current.addMessage(message);
        MessageHistory updated = new MessageHistory();
        updated.setMessages(current.getMessages());
        historyLiveData.postValue(updated);

        // Also update conversation summaries in real-time
        updateConversationSummariesFromMessage(message);
    }

    @Override
    public void onMessageSent(Message message) {
        onNewMessage(message); // will also update summaries
    }

    @Override
    public void onConversationHistory(String userId, List<Message> messages) {
        MessageHistory history = new MessageHistory();
        history.setMessages(messages);
        historyLiveData.postValue(history);
    }

    @Override
    public void onConversationsList(List<ConversationSummary> conversations) { conversationsLiveData.postValue(conversations); }
    @Override
    public void onConnected() { connectionStatus.postValue(true); setUserOnline(); requestConversationsList(); }

    @Override
    public void onUnreadCount(int count) { }

    @Override
    public void onMessagesRead(String readBy, String readAt) {
        // readBy is the other user who has read our messages. This does not affect our unread counts.
        // However, if WE read someone else's messages, the server notifies the sender (them). We need to reduce unreadCount when we mark messages read locally.
        // This event isn't triggered for our own reading, so we handle unread decrement elsewhere (when markMessagesAsRead is called successfully).
    }

    @Override
    public void onUserTyping(String userId, String userName) { }

    @Override
    public void onUserStoppedTyping(String userId) { }

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

    // --- Internal helpers ---
    private void updateConversationSummariesFromMessage(Message message) {
        if (message == null) return;
        List<ConversationSummary> current = conversationsLiveData.getValue();
        if (current == null) {
            current = new java.util.ArrayList<>();
        }

        // Determine the other participant id
        String selfId = currentUserId != null ? currentUserId : "";
        String otherUserId = message.getSenderId().equals(selfId) ? message.getRecipientId() : message.getSenderId();

        ConversationSummary target = null;
        for (ConversationSummary cs : current) {
            if (cs.getUserId().equals(otherUserId)) { target = cs; break; }
        }

        // Build updated summary (clone list to trigger LiveData observers)
        java.util.ArrayList<ConversationSummary> updated = new java.util.ArrayList<>(current.size() + (target == null ? 1 : 0));
        long nowOrder = System.currentTimeMillis(); // for potential sorting if needed

        // If existing, update fields and move to top
        if (target != null) {
            int unread = target.getUnreadCount();
            if (message.getRecipientId().equals(selfId) && !message.isRead()) {
                unread += 1; // increment unread for incoming message
            }
            ConversationSummary newSummary = new ConversationSummary(
                    target.getUserId(),
                    target.getUserName(),
                    message.getContent(),
                    message.getCreatedAt(),
                    unread
            );
            updated.add(newSummary); // add updated one first (top)
            for (ConversationSummary cs : current) {
                if (cs == target) continue; // skip old
                updated.add(cs);
            }
        } else {
            // New conversation not yet in list
            int unread = message.getRecipientId().equals(selfId) && !message.isRead() ? 1 : 0;
            String userName = getUserNameById(otherUserId);
            ConversationSummary newSummary = new ConversationSummary(
                    otherUserId,
                    userName, // Try to get userName from active connections, fall back to null if not found
                    message.getContent(),
                    message.getCreatedAt(),
                    unread
            );
            updated.add(newSummary);
            updated.addAll(current);
        }

        conversationsLiveData.postValue(updated);
    }
}
