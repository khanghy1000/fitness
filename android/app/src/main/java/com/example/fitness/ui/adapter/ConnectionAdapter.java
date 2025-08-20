package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.Connection;

import java.util.ArrayList;
import java.util.List;

public class ConnectionAdapter extends RecyclerView.Adapter<ConnectionAdapter.ConnectionViewHolder> {
    private List<Connection> connections = new ArrayList<>();
    private OnConnectionActionListener listener;
    private ConnectionType connectionType;

    public enum ConnectionType {
        ACTIVE_CONNECTION,
        ACTIVE_COACHES,
        RECEIVED_REQUEST,
        SENT_REQUEST
    }

    public interface OnConnectionActionListener {
        void onAcceptConnection(String traineeId);
        void onRejectConnection(String traineeId);
        void onDisconnectTrainee(String traineeId);
        void onTraineeClick(String traineeId, String traineeName);
    }

    public ConnectionAdapter(ConnectionType connectionType, OnConnectionActionListener listener) {
        this.connectionType = connectionType;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConnectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_connection, parent, false);
        return new ConnectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectionViewHolder holder, int position) {
        Connection connection = connections.get(position);
        holder.bind(connection, connectionType, listener);
    }

    @Override
    public int getItemCount() {
        return connections.size();
    }

    public void updateConnections(List<Connection> newConnections) {
        this.connections.clear();
        this.connections.addAll(newConnections);
        notifyDataSetChanged();
    }

    static class ConnectionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName;
        private TextView tvUserEmail;
        private TextView tvStatus;
        private TextView tvNotes;
        private TextView tvRequestDate;
        private Button btnAccept;
        private Button btnReject;
        private Button btnDisconnect;

        public ConnectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvNotes = itemView.findViewById(R.id.tv_notes);
            tvRequestDate = itemView.findViewById(R.id.tv_request_date);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnDisconnect = itemView.findViewById(R.id.btn_disconnect);
        }

        public void bind(Connection connection, ConnectionType connectionType, OnConnectionActionListener listener) {
            // Show appropriate user info based on connection type
            if (connectionType == ConnectionType.RECEIVED_REQUEST || connectionType == ConnectionType.ACTIVE_CONNECTION) {
                // For coach viewing trainee info
                tvUserName.setText(connection.getTrainee().getName());
                tvUserEmail.setText(connection.getTrainee().getEmail());
            } else {
                // For trainee viewing coach info (ACTIVE_COACHES, SENT_REQUEST)
                tvUserName.setText(connection.getCoach().getName());
                tvUserEmail.setText(connection.getCoach().getEmail());
            }

            tvStatus.setText("Status: " + connection.getStatus().getValue());
            tvRequestDate.setText("Requested: " + connection.getCreatedAt());
            
            if (connection.getNotes() != null && !connection.getNotes().isEmpty()) {
                tvNotes.setVisibility(View.VISIBLE);
                tvNotes.setText("Notes: " + connection.getNotes());
            } else {
                tvNotes.setVisibility(View.GONE);
            }

            // Add click listener for coach active connections to open trainee management
            if (connectionType == ConnectionType.ACTIVE_CONNECTION) {
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onTraineeClick(connection.getTraineeId(), connection.getTrainee().getName());
                    }
                });
                itemView.setClickable(true);
                itemView.setFocusable(true);
            } else {
                // Remove click listeners for other types (including ACTIVE_COACHES)
                itemView.setOnClickListener(null);
                itemView.setClickable(false);
                itemView.setFocusable(false);
            }

            // Configure buttons based on connection type
            switch (connectionType) {
                case RECEIVED_REQUEST:
                    btnAccept.setVisibility(View.VISIBLE);
                    btnReject.setVisibility(View.VISIBLE);
                    btnDisconnect.setVisibility(View.GONE);
                    
                    btnAccept.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onAcceptConnection(connection.getTraineeId());
                        }
                    });
                    
                    btnReject.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onRejectConnection(connection.getTraineeId());
                        }
                    });
                    break;

                case ACTIVE_CONNECTION:
                    btnAccept.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                    btnDisconnect.setVisibility(View.VISIBLE);
                    
                    btnDisconnect.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onDisconnectTrainee(connection.getTraineeId());
                        }
                    });
                    break;

                case ACTIVE_COACHES:
                    // For trainee viewing active coaches - no buttons should be shown
                    btnAccept.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                    btnDisconnect.setVisibility(View.GONE);
                    break;

                case SENT_REQUEST:
                default:
                    btnAccept.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                    btnDisconnect.setVisibility(View.GONE);
                    break;
            }
        }
    }
}
