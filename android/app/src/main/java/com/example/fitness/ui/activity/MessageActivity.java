package com.example.fitness.ui.activity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitness.R;
// Repository removed in refactor; using ViewModel directly
import com.example.fitness.databinding.ActivityMessageBinding;
import com.example.fitness.model.ConversationSummary;
import com.example.fitness.ui.adapter.MessageHistoryAdapter;
import com.example.fitness.ui.viewmodel.ConnectionsViewModel;
import com.example.fitness.ui.viewmodel.MessageHistoryViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import dagger.hilt.android.AndroidEntryPoint;

import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import com.example.fitness.data.local.AuthDataStore;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class MessageActivity extends AppCompatActivity implements MessageHistoryAdapter.OnMessageClickListener {
    private MessageHistoryAdapter adapter;
    private ActivityMessageBinding binding;
    private MessageHistoryViewModel viewModel;
    private ConnectionsViewModel connectionsViewModel;
    private String currentUserId;
    @Inject
    AuthDataStore authDataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this).get(MessageHistoryViewModel.class);
        connectionsViewModel = new ViewModelProvider(this).get(ConnectionsViewModel.class);
        connectToSocket();
        loadConversationIfProvided();
        initializeViews();
        setupRecyclerView();
        setupObservers();
        setupConnectionsObservers();
        fetchCurrentUserId();
        // Preload active connections if not already loaded (for permission checks)
        if (!connectionsViewModel.hasLoadedActiveConnections()) {
            connectionsViewModel.loadActiveConnections();
        }
    }

    private void initializeViews() {
        MaterialToolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // FAB: show connected users activity
        binding.fabAddNutritionPlan.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConnectedUsersActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        adapter = new MessageHistoryAdapter(this);

        RecyclerView recyclerView = binding.recyclerViewMessages;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add divider between items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Set your adapter here when you create it
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getConversations().observe(this, list -> {
            Log.d(TAG, "Observer conversations size=" + (list != null ? list.size() : 0));
            if (list != null) adapter.updateConversations(list);
        });
        viewModel.getConnectionStatus().observe(this, connected -> {
            if (Boolean.TRUE.equals(connected)) {
                Log.d(TAG, "Socket connected successfully");
                // Optionally load conversations if needed
            } else {
                Log.d(TAG, "Socket disconnected or not connected");
            }
        });
        viewModel.getErrors().observe(this, this::showError);
    }

    private void setupConnectionsObservers() {
        connectionsViewModel.errorMessage.observe(this, err -> {
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        });
    }

    private void fetchCurrentUserId() {
        authDataStore.getUserIdSync()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(id -> currentUserId = id, throwable -> Log.e(TAG, "Failed to fetch user id", throwable));
    }

    private void connectToSocket() {
        Log.d(TAG, "Attempting to connect to socket");
        if (!viewModel.isConnected()) {
            authDataStore.getJwtTokenSync()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(token -> Log.d(TAG, "DEBUG_TOKEN len=" + (token != null ? token.length() : 0) + " value=" + token),
                            throwable -> Log.e(TAG, "Failed to fetch token", throwable));
            viewModel.connect();
        } else {
            Log.d(TAG, "Socket already connected");
        }
    }

    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMessageClick(ConversationSummary summary) {
        // Enforce active connection restriction only after data loaded
        if (connectionsViewModel.hasLoadedActiveConnections() && !connectionsViewModel.isUserConnected(summary.getUserId())) {
            Toast.makeText(this, "You can only message connected users", Toast.LENGTH_LONG).show();
            connectionsViewModel.loadActiveConnections();
            return;
        } else if (!connectionsViewModel.hasLoadedActiveConnections()) {
            // Trigger load and delay action
            connectionsViewModel.loadActiveConnections();
            return;
        }
        android.content.Intent intent = new android.content.Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_USER_ID, summary.getUserId());
        intent.putExtra(ChatActivity.EXTRA_USER_NAME, summary.getUserName());
        startActivity(intent);
    }

    private void loadConversationIfProvided() {
        String targetUserId = getIntent().getStringExtra("userId");
        if (targetUserId != null) {
            viewModel.loadConversation(targetUserId, 50, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reconnect if disconnected
        if (!viewModel.isConnected()) {
            Log.d(TAG, "Reconnecting in onResume");
            connectToSocket();
        }
    }
}