package com.example.fitness.data.network;

import android.util.Log;

import com.example.fitness.data.local.AuthDataStore;
import com.example.fitness.model.ConversationRequest;
import com.example.fitness.model.ConversationSummary;
import com.example.fitness.model.Message;
import com.example.fitness.model.SendMessageRequest;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.socket.client.Socket;

@Singleton
public class SocketService {
    private static final String TAG = "SocketService";

    private final Socket socket;
    private final JsonAdapter<Message> messageAdapter;
    private final JsonAdapter<List<Message>> messageListAdapter;
    private final JsonAdapter<SendMessageRequest> sendMessageAdapter;
    private final JsonAdapter<ConversationRequest> conversationAdapter;
    private final JsonAdapter<List<ConversationSummary>> conversationSummaryListAdapter;
    private SocketEventListener eventListener;
    private final AuthDataStore authDataStore;

    public interface SocketEventListener {
        void onNewMessage(Message message);
        void onMessageSent(Message message);
        void onConversationHistory(String userId, List<Message> messages);
    void onConversationsList(List<ConversationSummary> conversations);
        void onUnreadCount(int count);
        void onMessagesRead(String readBy, String readAt);
        void onUserTyping(String userId, String userName);
        void onUserStoppedTyping(String userId);
        void onUserStatusChanged(String userId, String status);
        void onError(String error);
        void onConnected();
        void onDisconnected();
    }
    @Inject
    public SocketService(Socket socket,
                         JsonAdapter<Message> messageAdapter,
                         JsonAdapter<List<Message>> messageListAdapter,
                         Moshi moshi,
                         AuthDataStore authDataStore) {
        this.socket = socket;
        this.messageAdapter = messageAdapter;
        this.messageListAdapter = messageListAdapter;
        this.sendMessageAdapter = moshi.adapter(SendMessageRequest.class);
        this.conversationAdapter = moshi.adapter(ConversationRequest.class);
        this.conversationSummaryListAdapter = moshi.adapter(com.squareup.moshi.Types.newParameterizedType(List.class, ConversationSummary.class));
        this.authDataStore = authDataStore;

        setupEventListeners();
    }
    private void setupEventListeners() {
        // Additional low-level connection diagnostics
    socket.on("connect_error", args -> {
            Log.e(TAG, "Connect error: " + (args != null && args.length > 0 ? args[0] : "unknown"));
            if (eventListener != null) {
                eventListener.onError("connect_error: " + (args != null && args.length > 0 ? args[0] : ""));
            }
        });
    socket.on("connect_timeout", args -> {
            Log.e(TAG, "Connect timeout");
            if (eventListener != null) {
                eventListener.onError("connect_timeout");
            }
        });
    socket.on("reconnect_attempt", args -> Log.d(TAG, "Reconnecting attempt..."));
    socket.on("reconnect_error", args -> Log.e(TAG, "Reconnect error: " + (args != null && args.length > 0 ? args[0] : "")));
    socket.on("reconnect_failed", args -> {
            Log.e(TAG, "Reconnect failed");
            if (eventListener != null) {
                eventListener.onDisconnected();
            }
        });
        socket.on(Socket.EVENT_CONNECT, args -> {
            Log.d(TAG, "Socket connected");
            if (eventListener != null) {
                eventListener.onConnected();
            }
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            Log.d(TAG, "Socket disconnected");
            if (eventListener != null) {
                eventListener.onDisconnected();
            }
        });

        socket.on("new_message", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                Message message = messageAdapter.fromJson(data.toString());
                if (eventListener != null && message != null) {
                    eventListener.onNewMessage(message);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing new message", e);
            }
        });

        socket.on("message_sent", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                Message message = messageAdapter.fromJson(data.toString());
                if (eventListener != null && message != null) {
                    eventListener.onMessageSent(message);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing message sent", e);
            }
        });

        socket.on("conversation_history", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String userId = data.getString("userId");
                String messagesJson = data.getJSONArray("messages").toString();
                List<Message> messages = messageListAdapter.fromJson(messagesJson);

                if (eventListener != null) {
                    eventListener.onConversationHistory(userId, messages);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing conversation history", e);
            }
        });

        socket.on("conversations_list", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String listJson = data.getJSONArray("conversations").toString();
                List<ConversationSummary> list = conversationSummaryListAdapter.fromJson(listJson);
                Log.d(TAG, "Received conversations_list size=" + (list != null ? list.size() : 0));
                if (eventListener != null) {
                    eventListener.onConversationsList(list != null ? list : Collections.emptyList());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing conversations list", e);
            }
        });

        socket.on("unread_count", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                int count = data.getInt("count");
                if (eventListener != null) {
                    eventListener.onUnreadCount(count);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing unread count", e);
            }
        });
        socket.on("messages_read", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String readBy = data.getString("readBy");
                String readAt = data.getString("readAt");
                if (eventListener != null) {
                    eventListener.onMessagesRead(readBy, readAt);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing messages read", e);
            }
        });

        socket.on("user_typing", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String userId = data.getString("userId");
                String userName = data.getString("userName");
                if (eventListener != null) {
                    eventListener.onUserTyping(userId, userName);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing user typing", e);
            }
        });

        socket.on("user_stopped_typing", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String userId = data.getString("userId");
                if (eventListener != null) {
                    eventListener.onUserStoppedTyping(userId);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing user stopped typing", e);
            }
        });

        socket.on("user_status_changed", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String userId = data.getString("userId");
                String status = data.getString("status");
                if (eventListener != null) {
                    eventListener.onUserStatusChanged(userId, status);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing user status changed", e);
            }
        });

        socket.on("error", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String message = data.getString("message");
                if (eventListener != null) {
                    eventListener.onError(message);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing error message", e);
            }
        });
    }
    public void connect() {
        if (!socket.connected()) {
            socket.connect();
            Log.d(TAG, "Connecting to socket");
        }
        else {
            Log.d(TAG, "Socket already connected");
        }
    }
    public void disconnect() {
        if (socket.connected()) {
            socket.disconnect();
        }
    }
    public void sendMessage(String recipientId, String content, String replyToId) {
        try {
            SendMessageRequest request = new SendMessageRequest(recipientId, content, replyToId);
            String json = sendMessageAdapter.toJson(request);
            JSONObject data = new JSONObject(json);
            socket.emit("send_message", data);
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
        }
    }
    public void getConversation(String userId, int limit, int offset) {
        try {
            ConversationRequest request = new ConversationRequest(userId, limit, offset);
            String json = conversationAdapter.toJson(request);
            JSONObject data = new JSONObject(json);
            socket.emit("get_conversation", data);
        } catch (Exception e) {
            Log.e(TAG, "Error getting conversation", e);
        }
    }
    public void markMessagesAsRead(String conversationUserId) {
        try {
            JSONObject data = new JSONObject();
            data.put("conversationUserId", conversationUserId);
            socket.emit("mark_messages_read", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error marking messages as read", e);
        }
    }
    public void getConversationsList() {
    Log.d(TAG, "Requesting conversations list");
        socket.emit("get_conversations");
    }
    public void getUnreadCount() {
        socket.emit("get_unread_count");
    }
    public void startTyping(String recipientId) {
        try {
            JSONObject data = new JSONObject();
            data.put("recipientId", recipientId);
            socket.emit("typing_start", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error sending typing start", e);
        }
    }

    public void stopTyping(String recipientId) {
        try {
            JSONObject data = new JSONObject();
            data.put("recipientId", recipientId);
            socket.emit("typing_stop", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error sending typing stop", e);
        }
    }

    public void setUserOnline() {
        socket.emit("user_online");
    }

    public void setEventListener(SocketEventListener listener) {
        this.eventListener = listener;
    }

    public void removeEventListener() {
        this.eventListener = null;
    }

    public boolean isConnected() {
        return socket.connected();
    }
}
