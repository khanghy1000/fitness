package com.example.fitness.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitness.R;
import com.example.fitness.databinding.ActivityConnectedUsersBinding;
import com.example.fitness.ui.adapter.ConnectedUsersAdapter;
import com.example.fitness.ui.viewmodel.ConnectionsViewModel;
import com.example.fitness.data.local.AuthDataStore;

import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ConnectedUsersActivity extends AppCompatActivity implements ConnectedUsersAdapter.OnUserClickListener {

    private ActivityConnectedUsersBinding binding;
    private ConnectionsViewModel viewModel;
    private ConnectedUsersAdapter adapter;
    private String currentUserId;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    AuthDataStore authDataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectedUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupToolbar();
        setupRecyclerView();
        setupSearchFunctionality();
        setupViewModel();
        fetchCurrentUserId();
        loadConnectedUsers();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        adapter = new ConnectedUsersAdapter(this);
        binding.recyclerViewConnectedUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewConnectedUsers.setAdapter(adapter);
    }

    private void setupSearchFunctionality() {
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ConnectionsViewModel.class);
        
        viewModel.activeConnections.observe(this, connections -> {
            if (connections != null) {
                adapter.updateConnections(connections);
                updateEmptyState();
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.errorMessage.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                updateEmptyState();
            }
        });
    }

    private void fetchCurrentUserId() {
        disposables.add(
                authDataStore.getUserIdSync()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(id -> {
                            if (id != null && !id.isEmpty()) {
                                currentUserId = id;
                                adapter.setCurrentUserId(id);
                            }
                        }, throwable -> {
                            Toast.makeText(this, "Failed to fetch user ID", Toast.LENGTH_SHORT).show();
                        })
        );
    }

    private void loadConnectedUsers() {
        viewModel.loadActiveConnections();
    }

    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        binding.layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerViewConnectedUsers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        
        // Update empty message based on search state
        String searchQuery = binding.editTextSearch.getText().toString().trim();
        if (!searchQuery.isEmpty() && isEmpty) {
            binding.textViewEmptyMessage.setText("No users found matching \"" + searchQuery + "\"");
        } else if (isEmpty) {
            binding.textViewEmptyMessage.setText("No connected users found");
        }
    }

    @Override
    public void onUserClick(ConnectedUsersAdapter.ConnectedUserItem user) {
        // Verify the user is still connected before opening chat
        if (!viewModel.isUserConnected(user.getUserId())) {
            loadConnectedUsers(); // Refresh the list
            return;
        }

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_USER_ID, user.getUserId());
        intent.putExtra(ChatActivity.EXTRA_USER_NAME, user.getUserName());
        startActivity(intent);
        
        // Optionally finish this activity to go back to messages
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh connections when returning to this screen
        if (viewModel != null) {
            loadConnectedUsers();
        }
    }
}
