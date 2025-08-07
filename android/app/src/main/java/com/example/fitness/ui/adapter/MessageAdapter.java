package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.model.MessageHistory;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<MessageHistory> messages = new ArrayList<>();
    private OnMessageClickListener listener;
    public interface OnMessageClickListener {
        void onMessageClick(MessageHistory message);
    }
    public MessageAdapter(OnMessageClickListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageHistory message = messages.get(position);
        holder.bind(message, listener);
    }
    @Override
    public int getItemCount() {
        return messages.size();
    }

    // Dummy updateMessages method to simulate data loading
    public void updateMessages(List<MessageHistory> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
        notifyDataSetChanged();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewSenderName;
        private TextView textViewMessageContent;
        private TextView textViewTimestamp;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSenderName = itemView.findViewById(R.id.textViewName);
            textViewMessageContent = itemView.findViewById(R.id.textViewLastMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTime);
        }

        public void bind(MessageHistory message, OnMessageClickListener listener) {
            textViewSenderName.setText(message.getSenderName());
            textViewMessageContent.setText(message.getMessageContent());
            textViewTimestamp.setText(message.getTimestamp());
            itemView.setOnClickListener(v -> listener.onMessageClick(message));
        }
    }
}
