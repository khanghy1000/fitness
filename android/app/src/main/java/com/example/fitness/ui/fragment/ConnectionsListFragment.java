package com.example.fitness.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness.databinding.FragmentConnectionsListBinding;
import com.example.fitness.ui.adapter.ConnectionAdapter;
import com.example.fitness.ui.viewmodel.ConnectionsViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ConnectionsListFragment extends Fragment implements ConnectionAdapter.OnConnectionActionListener {
    private static final String ARG_CONNECTION_TYPE = "connection_type";
    
    private FragmentConnectionsListBinding binding;
    private ConnectionsViewModel viewModel;
    private ConnectionAdapter adapter;
    private ConnectionAdapter.ConnectionType connectionType;

    public static ConnectionsListFragment newInstance(ConnectionAdapter.ConnectionType connectionType) {
        ConnectionsListFragment fragment = new ConnectionsListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONNECTION_TYPE, connectionType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            connectionType = (ConnectionAdapter.ConnectionType) getArguments().getSerializable(ARG_CONNECTION_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConnectionsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(ConnectionsViewModel.class);
        
        setupRecyclerView();
        observeViewModel();
        loadData();
    }

    private void setupRecyclerView() {
        adapter = new ConnectionAdapter(connectionType, this);
        binding.rvConnections.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvConnections.setAdapter(adapter);
    }

    private void observeViewModel() {
        switch (connectionType) {
            case ACTIVE_CONNECTION:
                viewModel.activeConnections.observe(getViewLifecycleOwner(), connections -> {
                    if (connections != null && !connections.isEmpty()) {
                        adapter.updateConnections(connections);
                        binding.tvEmptyState.setVisibility(View.GONE);
                        binding.rvConnections.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmptyState.setVisibility(View.VISIBLE);
                        binding.rvConnections.setVisibility(View.GONE);
                        binding.tvEmptyState.setText("No active connections found");
                    }
                });
                break;
                
            case SENT_REQUEST:
                viewModel.sentRequests.observe(getViewLifecycleOwner(), connections -> {
                    if (connections != null && !connections.isEmpty()) {
                        adapter.updateConnections(connections);
                        binding.tvEmptyState.setVisibility(View.GONE);
                        binding.rvConnections.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmptyState.setVisibility(View.VISIBLE);
                        binding.rvConnections.setVisibility(View.GONE);
                        binding.tvEmptyState.setText("No sent requests found");
                    }
                });
                break;
                
            case RECEIVED_REQUEST:
                viewModel.receivedRequests.observe(getViewLifecycleOwner(), connections -> {
                    if (connections != null && !connections.isEmpty()) {
                        adapter.updateConnections(connections);
                        binding.tvEmptyState.setVisibility(View.GONE);
                        binding.rvConnections.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmptyState.setVisibility(View.VISIBLE);
                        binding.rvConnections.setVisibility(View.GONE);
                        binding.tvEmptyState.setText("No connection requests found");
                    }
                });
                break;
        }
    }

    private void loadData() {
        switch (connectionType) {
            case ACTIVE_CONNECTION:
                viewModel.loadActiveConnections();
                break;
            case SENT_REQUEST:
                viewModel.loadSentRequests();
                break;
            case RECEIVED_REQUEST:
                viewModel.loadReceivedRequests();
                break;
        }
    }

    @Override
    public void onAcceptConnection(String traineeId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Accept Connection Request")
                .setMessage("Are you sure you want to accept this trainee's connection request?")
                .setPositiveButton("Accept", (dialog, which) -> {
                    viewModel.acceptConnectionRequest(traineeId);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRejectConnection(String traineeId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reject Connection Request")
                .setMessage("Are you sure you want to reject this trainee's connection request? The trainee will be notified.")
                .setPositiveButton("Reject", (dialog, which) -> {
                    viewModel.rejectConnectionRequest(traineeId);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDisconnectTrainee(String traineeId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Disconnect Trainee")
                .setMessage("Are you sure you want to disconnect this trainee? This action cannot be undone.")
                .setPositiveButton("Disconnect", (dialog, which) -> {
                    viewModel.disconnectTrainee(traineeId);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
