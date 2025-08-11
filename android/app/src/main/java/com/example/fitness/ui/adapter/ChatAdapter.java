package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitness.R;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_INCOMING = 0;
    private static final int VIEW_TYPE_OUTGOING = 1;
    private final List<ChatMessage> messages;
    private String currentUserId;

    public ChatAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
        notifyDataSetChanged();
    }

    public void setMessages(List<ChatMessage> newMessages) {
        this.messages.clear();
        if (newMessages != null) this.messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage message) {
        if (message != null) {
            this.messages.add(message);
            notifyItemInserted(this.messages.size() - 1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        return message.getSenderId().equals(currentUserId) ? VIEW_TYPE_OUTGOING : VIEW_TYPE_INCOMING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_OUTGOING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_outgoing, parent, false);
            return new OutgoingViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_incoming, parent, false);
            return new IncomingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof OutgoingViewHolder) {
            ((OutgoingViewHolder) holder).bind(message);
        } else if (holder instanceof IncomingViewHolder) {
            ((IncomingViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class IncomingViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSender;
        TextView textViewMessage;
        TextView textViewTimestamp;
        IncomingViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSender = itemView.findViewById(R.id.textViewSender);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
        }
        void bind(ChatMessage message) {
            textViewSender.setText(message.getSenderName());
            textViewMessage.setText(message.getText());
            if (textViewTimestamp != null) textViewTimestamp.setText(message.getDisplayTime());
        }
    }

    static class OutgoingViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSender;
        TextView textViewMessage;
        TextView textViewTimestamp;
        TextView textViewReadStatus;
        OutgoingViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSender = itemView.findViewById(R.id.textViewSender);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            textViewReadStatus = itemView.findViewById(R.id.textViewReadStatus);
        }
        void bind(ChatMessage message) {
            textViewSender.setText(message.getSenderName());
            textViewMessage.setText(message.getText());
            if (textViewTimestamp != null) textViewTimestamp.setText(message.getDisplayTime());
            if (textViewReadStatus != null) {
                String status = message.isRead ? "Read" : (message.readAt != null ? "Delivered" : "Sent");
                textViewReadStatus.setText(" • " + status);
            }
        }
    }

    public static class ChatMessage {
        private final String senderId;
        private final String senderName;
        private final String text;
        private final String createdAt;
        private final boolean isRead;
        private final String readAt;
        public ChatMessage(String senderId, String senderName, String text, String createdAt, boolean isRead, String readAt) {
            this.senderId = senderId;
            this.senderName = senderName;
            this.text = text;
            this.createdAt = createdAt;
            this.isRead = isRead;
            this.readAt = readAt;
        }
        public String getSenderId() { return senderId; }
        public String getSenderName() { return senderName; }
        public String getText() { return text; }
    public boolean isRead() { return isRead; }
        public String getDisplayTime() {
            if (createdAt == null) return "";
            // Expecting ISO 8601; keep simple parsing
            try {
                java.text.SimpleDateFormat iso = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                iso.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                java.util.Date date = iso.parse(createdAt);
                java.text.SimpleDateFormat display = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault());
                return display.format(date);
            } catch (Exception e) { return ""; }
        }
    }
}
