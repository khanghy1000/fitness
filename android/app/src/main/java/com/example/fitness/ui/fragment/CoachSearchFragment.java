package com.example.fitness.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitness.data.network.model.generated.User;
import com.example.fitness.data.network.retrofit.UsersApi;
import com.example.fitness.data.repository.UsersRepository;
import com.example.fitness.databinding.FragmentCoachSearchBinding;
import com.example.fitness.ui.adapter.CoachSearchAdapter;
import com.example.fitness.ui.viewmodel.ConnectionsViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CoachSearchFragment extends Fragment implements CoachSearchAdapter.OnCoachActionListener {
    
    private FragmentCoachSearchBinding binding;
    private ConnectionsViewModel connectionsViewModel;
    private CoachSearchAdapter adapter;
    
    @Inject
    UsersRepository usersRepository;

    public static CoachSearchFragment newInstance() {
        return new CoachSearchFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCoachSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        connectionsViewModel = new ViewModelProvider(requireActivity()).get(ConnectionsViewModel.class);
        
        setupRecyclerView();
        setupSearchField();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new CoachSearchAdapter(this);
        binding.rvCoaches.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCoaches.setAdapter(adapter);
    }

    private void setupSearchField() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    searchCoaches(query);
                } else if (query.isEmpty()) {
                    adapter.updateCoaches(List.of());
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                    binding.tvEmptyState.setText("Search for coaches to connect with");
                }
            }
        });
    }

    private void observeViewModel() {
        connectionsViewModel.successMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                connectionsViewModel.clearMessages();
            }
        });

        connectionsViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                connectionsViewModel.clearMessages();
            }
        });
    }

    private void searchCoaches(String query) {
        usersRepository.searchUsers(query, UsersApi.RoleApiUsersSearchGet.coach, new UsersRepository.UsersCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                if (result != null && !result.isEmpty()) {
                    adapter.updateCoaches(result);
                    binding.tvEmptyState.setVisibility(View.GONE);
                    binding.rvCoaches.setVisibility(View.VISIBLE);
                } else {
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                    binding.rvCoaches.setVisibility(View.GONE);
                    binding.tvEmptyState.setText("No coaches found");
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Search failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSendRequest(User coach) {
        showConnectionRequestDialog(coach);
    }

    private void showConnectionRequestDialog(User coach) {
        View dialogView = LayoutInflater.from(getContext()).inflate(com.example.fitness.R.layout.dialog_connection_request, null);
        TextInputLayout tilNotes = dialogView.findViewById(com.example.fitness.R.id.til_notes);
        TextInputEditText etNotes = dialogView.findViewById(com.example.fitness.R.id.et_notes);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Send Connection Request")
                .setMessage("Send a connection request to " + coach.getName() + "?")
                .setView(dialogView)
                .setPositiveButton("Send", (dialog, which) -> {
                    String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
                    connectionsViewModel.sendConnectionRequest(coach.getId(), notes);
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
