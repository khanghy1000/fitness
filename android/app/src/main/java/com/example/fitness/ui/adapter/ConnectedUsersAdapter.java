package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitness.databinding.ItemConnectedUserBinding;
import com.example.fitness.data.network.model.generated.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConnectedUsersAdapter extends RecyclerView.Adapter<ConnectedUsersAdapter.ConnectedUserViewHolder> {
    
    private List<ConnectedUserItem> allUsers = new ArrayList<>();
    private List<ConnectedUserItem> filteredUsers = new ArrayList<>();
    private String currentUserId;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(ConnectedUserItem user);
    }

    public static class ConnectedUserItem {
        private final String userId;
        private final String userName;
        private final String role;
        private final boolean isCoach;

        public ConnectedUserItem(String userId, String userName, String role, boolean isCoach) {
            this.userId = userId;
            this.userName = userName;
            this.role = role;
            this.isCoach = isCoach;
        }

        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getRole() { return role; }
        public boolean isCoach() { return isCoach; }
    }

    public ConnectedUsersAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void updateConnections(List<Connection> connections) {
        allUsers.clear();
        
        if (connections != null && currentUserId != null) {
            for (Connection connection : connections) {
                try {
                    String otherId;
                    String otherName;
                    String role;
                    boolean isCoach;
                    
                    if (currentUserId.equals(connection.getCoachId())) {
                        // Current user is coach, other is trainee
                        otherId = connection.getTraineeId();
                        otherName = connection.getTrainee() != null ? 
                            connection.getTrainee().getName() : otherId;
                        role = "Trainee";
                        isCoach = false;
                    } else if (currentUserId.equals(connection.getTraineeId())) {
                        // Current user is trainee, other is coach
                        otherId = connection.getCoachId();
                        otherName = connection.getCoach() != null ? 
                            connection.getCoach().getName() : otherId;
                        role = "Coach";
                        isCoach = true;
                    } else {
                        // Fallback: treat trainee as other user
                        otherId = connection.getTraineeId();
                        otherName = connection.getTrainee() != null ? 
                            connection.getTrainee().getName() : otherId;
                        role = "Trainee";
                        isCoach = false;
                    }
                    
                    if (otherId != null && otherName != null) {
                        allUsers.add(new ConnectedUserItem(otherId, otherName, role, isCoach));
                    }
                } catch (Exception e) {
                    // Skip malformed connection
                }
            }
        }
        
        filteredUsers.clear();
        filteredUsers.addAll(allUsers);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredUsers.clear();
        
        if (query == null || query.trim().isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String lowerQuery = query.toLowerCase(Locale.getDefault());
            for (ConnectedUserItem user : allUsers) {
                if (user.getUserName().toLowerCase(Locale.getDefault()).contains(lowerQuery)) {
                    filteredUsers.add(user);
                }
            }
        }
        
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConnectedUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemConnectedUserBinding binding = ItemConnectedUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ConnectedUserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectedUserViewHolder holder, int position) {
        ConnectedUserItem user = filteredUsers.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }

    class ConnectedUserViewHolder extends RecyclerView.ViewHolder {
        private final ItemConnectedUserBinding binding;

        public ConnectedUserViewHolder(ItemConnectedUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ConnectedUserItem user) {
            binding.textViewName.setText(user.getUserName());
            binding.textViewRole.setText(user.getRole().toUpperCase());
            binding.textViewStatus.setText("Connected");
            
            // Set initial letter for avatar
            String initial = user.getUserName().length() > 0 ? 
                String.valueOf(user.getUserName().charAt(0)).toUpperCase() : "?";
            binding.textViewInitial.setText(initial);
            
            // Set role chip color based on role
            if (user.isCoach()) {
                binding.textViewRole.setBackgroundResource(com.example.fitness.R.drawable.bg_coach_chip);
            } else {
                binding.textViewRole.setBackgroundResource(com.example.fitness.R.drawable.bg_role_chip);
            }
            
            // Set click listener
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });
        }
    }
}
