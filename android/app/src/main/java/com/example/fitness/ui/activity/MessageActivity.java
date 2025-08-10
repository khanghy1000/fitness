package com.example.fitness.ui.activity;

import static android.content.ContentValues.TAG;

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
    @Inject AuthDataStore authDataStore;
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
    connectToSocket();
    loadConversationIfProvided();
        initializeViews();
        setupRecyclerView();
        setupObservers();
    }
    private void initializeViews() {
        MaterialToolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
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
        Toast.makeText(this, "Clicked conversation with: " + summary.getUserName(), Toast.LENGTH_SHORT).show();
        // TODO: navigate to detail chat using summary.getUserId()
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