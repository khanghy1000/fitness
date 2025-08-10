package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.SocketService;
import com.example.fitness.model.ConversationSummary;
import com.example.fitness.model.Message;
import com.example.fitness.model.MessageHistory;

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

    @Inject
    public MessageHistoryViewModel(SocketService socketService) {
        this.socketService = socketService;
        this.socketService.setEventListener(this);
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
    public void markMessagesAsRead(String conversationUserId) { socketService.markMessagesAsRead(conversationUserId); }
    public void startTyping(String recipientId) { socketService.startTyping(recipientId); }
    public void stopTyping(String recipientId) { socketService.stopTyping(recipientId); }
    public void setUserOnline() { socketService.setUserOnline(); }
    public void requestUnreadCount() { socketService.getUnreadCount(); }
    public void requestConversationsList() { socketService.getConversationsList(); }

    // Socket callbacks
    @Override
    public void onNewMessage(Message message) {
        MessageHistory current = historyLiveData.getValue();
        if (current == null) current = new MessageHistory();
        current.addMessage(message);
        MessageHistory updated = new MessageHistory();
        updated.setMessages(current.getMessages());
        historyLiveData.postValue(updated);
    }

    @Override
    public void onMessageSent(Message message) {
        onNewMessage(message);
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
    public void onMessagesRead(String readBy, String readAt) { }

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
        socketService.removeEventListener();
    }
}
