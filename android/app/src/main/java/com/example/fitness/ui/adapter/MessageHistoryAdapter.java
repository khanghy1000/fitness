package com.example.fitness.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.model.ConversationSummary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lombok.NonNull;

public class MessageHistoryAdapter extends RecyclerView.Adapter<MessageHistoryAdapter.MessageViewHolder> {
    private static final String TAG = "MsgHistoryAdapter";
    private List<ConversationSummary> conversations = new ArrayList<>();
    private OnMessageClickListener listener;

    public interface OnMessageClickListener {
        void onMessageClick(ConversationSummary summary);
    }

    public MessageHistoryAdapter(OnMessageClickListener listener) {
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
        ConversationSummary summary = conversations.get(position);
        holder.bind(summary, listener);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public void updateConversations(List<ConversationSummary> list) {
        Log.d(TAG, "updateConversations newSize=" + (list != null ? list.size() : 0));
        this.conversations.clear();
        if (list != null) this.conversations.addAll(list);
        notifyDataSetChanged();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewSenderName;
        private TextView textViewMessageContent;
        private TextView textViewTimestamp;
        private View viewReadDot;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSenderName = itemView.findViewById(R.id.textViewName);
            textViewMessageContent = itemView.findViewById(R.id.textViewLastMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTime);
            viewReadDot = itemView.findViewById(R.id.viewReadDot);
        }

        public void bind(ConversationSummary summary, OnMessageClickListener listener) {
            textViewSenderName.setText(summary.getUserName() != null ? summary.getUserName() : summary.getUserId());
            textViewMessageContent.setText(summary.getLastMessage());
            textViewTimestamp.setText(formatTimestamp(summary.getLastMessageAt()));
            Log.d(TAG, "bind: unreadCount=" + summary.getUnreadCount());
            if (summary.getUnreadCount() > 0) {
                viewReadDot.setVisibility(View.VISIBLE);
            } else {
                viewReadDot.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMessageClick(summary);
                }
            });
        }

    private String formatTimestamp(String timestamp) {
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat displayFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                Date date = isoFormat.parse(timestamp);
                return displayFormat.format(date);
            } catch (ParseException e) {
                return timestamp; // Return original if parsing fails
            }
        }
    }
}