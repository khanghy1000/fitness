package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.User;

import java.util.ArrayList;
import java.util.List;

public class CoachSearchAdapter extends RecyclerView.Adapter<CoachSearchAdapter.CoachViewHolder> {
    private List<User> coaches = new ArrayList<>();
    private OnCoachActionListener listener;

    public interface OnCoachActionListener {
        void onSendRequest(User coach);
    }

    public CoachSearchAdapter(OnCoachActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CoachViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coach_search, parent, false);
        return new CoachViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoachViewHolder holder, int position) {
        User coach = coaches.get(position);
        holder.bind(coach, listener);
    }

    @Override
    public int getItemCount() {
        return coaches.size();
    }

    public void updateCoaches(List<User> newCoaches) {
        this.coaches.clear();
        this.coaches.addAll(newCoaches);
        notifyDataSetChanged();
    }

    static class CoachViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCoachName;
        private TextView tvCoachEmail;
        private Button btnSendRequest;

        public CoachViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCoachName = itemView.findViewById(R.id.tv_coach_name);
            tvCoachEmail = itemView.findViewById(R.id.tv_coach_email);
            btnSendRequest = itemView.findViewById(R.id.btn_send_request);
        }

        public void bind(User coach, OnCoachActionListener listener) {
            tvCoachName.setText(coach.getName());
            tvCoachEmail.setText(coach.getEmail());
            
            btnSendRequest.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSendRequest(coach);
                }
            });
        }
    }
}
