package com.example.fitness.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitness.R;
import com.example.fitness.databinding.ActivityChatBinding;
import com.example.fitness.model.Message;
import com.example.fitness.ui.adapter.ChatAdapter;
import com.example.fitness.ui.viewmodel.ChatViewModel;
import com.example.fitness.ui.viewmodel.ConnectionsViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

import com.example.fitness.data.local.AuthDataStore;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ChatActivity extends AppCompatActivity {
    public static final String EXTRA_USER_ID = "userId";
    public static final String EXTRA_USER_NAME = "userName";

    private ActivityChatBinding binding;
    private ChatViewModel viewModel;
    private ConnectionsViewModel connectionsViewModel;
    private ChatAdapter adapter;
    private String remoteUserId;
    private String remoteUserName;
    private String currentUserId; // Derived from messages (sender/recipient); optional improvement: store from auth
    @Inject
    AuthDataStore authDataStore;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parseIntent(getIntent());
        connectionsViewModel = new ViewModelProvider(this).get(ConnectionsViewModel.class);
        setupToolbar();
        setupRecycler();
        setupKeyboardAwareScrolling();
        setupViewModel();
        setupSendUI();
        observeViewModel();
        fetchCurrentUserId();

        // Enforce only active connections can chat
        if (!connectionsViewModel.hasLoadedActiveConnections()) {
            connectionsViewModel.loadActiveConnections();
        }
        connectionsViewModel.activeConnectionUserIds.observe(this, set -> {
            if (remoteUserId == null || set == null) return; // wait for data
            if (connectionsViewModel.hasLoadedActiveConnections() && !set.contains(remoteUserId)) {
                Toast.makeText(this, "You are not connected with this user", Toast.LENGTH_LONG).show();
                finish();
            } else if (set.contains(remoteUserId)) {
                // Try to get the user name from connections if not already set
                if (remoteUserName == null || remoteUserName.equals(remoteUserId)) {
                    String nameFromConnections = getUserNameFromConnections(remoteUserId);
                    if (nameFromConnections != null) {
                        remoteUserName = nameFromConnections;
                        setupToolbar(); // Update the toolbar title with the correct name
                    }
                }
                
                if (!viewModel.isConnected()) viewModel.connect();
                viewModel.init(remoteUserId);
                viewModel.loadConversation(50, 0);
                viewModel.markMessagesRead();
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
                        }, throwable -> { /* ignore */ })
        );
    }

    private void parseIntent(Intent intent) {
        remoteUserId = intent.getStringExtra(EXTRA_USER_ID);
        remoteUserName = intent.getStringExtra(EXTRA_USER_NAME);
        if (remoteUserName == null) remoteUserName = remoteUserId;
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = binding.toolbar;
        toolbar.setTitle(remoteUserName != null ? remoteUserName : "Chat");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private String getUserNameFromConnections(String userId) {
        if (connectionsViewModel == null || currentUserId == null || userId == null) {
            return null;
        }
        
        List<com.example.fitness.data.network.model.generated.Connection> connections = 
            connectionsViewModel.activeConnections.getValue();
        
        if (connections == null) {
            return null;
        }
        
        for (com.example.fitness.data.network.model.generated.Connection connection : connections) {
            try {
                if (currentUserId.equals(connection.getCoachId()) && userId.equals(connection.getTraineeId())) {
                    // Current user is coach, looking for trainee name
                    return connection.getTrainee() != null ? connection.getTrainee().getName() : null;
                } else if (currentUserId.equals(connection.getTraineeId()) && userId.equals(connection.getCoachId())) {
                    // Current user is trainee, looking for coach name
                    return connection.getCoach() != null ? connection.getCoach().getName() : null;
                }
            } catch (Exception ignored) {
                // Ignore any errors accessing connection properties
            }
        }
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecycler() {
        adapter = new ChatAdapter(new ArrayList<>(), ""); // currentUserId unknown initially
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true); // keep view anchored at bottom as messages grow
        binding.recyclerViewChat.setLayoutManager(lm);
        binding.recyclerViewChat.setAdapter(adapter);
    }

    private void setupKeyboardAwareScrolling() {
        // When layout changes (keyboard show/hide), scroll to last item
        binding.recyclerViewChat.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) { // likely keyboard shown
                binding.recyclerViewChat.postDelayed(() -> {
                    int count = adapter.getItemCount();
                    if (count > 0) binding.recyclerViewChat.scrollToPosition(count - 1);
                }, 50);
            }
        });
        // Also ensure EditText focuses visible
        binding.editTextMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.recyclerViewChat.postDelayed(() -> {
                    int count = adapter.getItemCount();
                    if (count > 0) binding.recyclerViewChat.scrollToPosition(count - 1);
                }, 100);
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(this, this::onMessagesUpdated);
        viewModel.getErrors().observe(this, err -> {
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        });
        viewModel.getTypingRemote().observe(this, typing -> {
            if (Boolean.TRUE.equals(typing)) {
                binding.editTextMessage.setHint(remoteUserName + " is typing...");
            } else {
                binding.editTextMessage.setHint("Type a message");
            }
        });
    }

    private void setupSendUI() {
        binding.buttonSend.setOnClickListener(v -> {
            String text = binding.editTextMessage.getText().toString();
            viewModel.sendMessage(text);
            binding.editTextMessage.setText("");
            viewModel.stopTyping();
        });

        binding.editTextMessage.addTextChangedListener(new TextWatcher() {
            private boolean typing = false;
            private final Runnable stopTypingRunnable = () -> {
                typing = false;
                viewModel.stopTyping();
            };

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!typing && s.length() > 0) {
                    typing = true;
                    viewModel.startTyping();
                }
                if (s.length() == 0 && typing) {
                    typing = false;
                    viewModel.stopTyping();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void onMessagesUpdated(List<Message> messages) {
        if (messages == null) return;
        // Determine current user id (assumes consistent sender/recipient pairing)
        if (currentUserId == null) {
            for (Message m : messages) {
                if (remoteUserId.equals(m.getSenderId())) {
                    currentUserId = m.getRecipientId();
                    break;
                } else if (remoteUserId.equals(m.getRecipientId())) {
                    currentUserId = m.getSenderId();
                    break;
                }
            }
            if (currentUserId == null) currentUserId = "me"; // fallback when no messages yet
            adapter.setCurrentUserId(currentUserId);
        }

        // Ensure chronological order oldest -> newest so list flows top to bottom
        List<Message> sorted = new ArrayList<>(messages);
        java.util.Collections.sort(sorted, (a, b) -> {
            String ca = a.getCreatedAt();
            String cb = b.getCreatedAt();
            if (ca == null || cb == null) return 0;
            return ca.compareTo(cb); // ISO8601 string compare works
        });

        List<ChatAdapter.ChatMessage> uiList = new ArrayList<>();
        for (Message m : sorted) {
            String senderName = m.getSenderId().equals(currentUserId) ? "You" : remoteUserName;
            uiList.add(new ChatAdapter.ChatMessage(m.getSenderId(), senderName, m.getContent(), m.getCreatedAt(), m.isRead(), m.getReadAt()));
        }
        adapter.setMessages(uiList);
        binding.recyclerViewChat.scrollToPosition(uiList.size() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.disconnect();
        disposables.clear();
    }
}
