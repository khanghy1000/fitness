package com.example.fitness.ui.activity;

import android.os.Bundle;
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
import com.example.fitness.databinding.ActivityMessageBinding;
import com.example.fitness.model.MessageHistory;
import com.example.fitness.ui.adapter.MessageAdapter;
import com.google.android.material.appbar.MaterialToolbar;

public class MessageActivity extends AppCompatActivity implements MessageAdapter.OnMessageClickListener {
    private MessageAdapter adapter;
    private ActivityMessageBinding binding;
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
        initializeViews();
        setupRecyclerView();
        // Load messages from the database or API
        loadMessages();
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
        adapter = new MessageAdapter(this);

        RecyclerView recyclerView = binding.recyclerViewMessages;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add divider between items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Set your adapter here when you create it
        recyclerView.setAdapter(adapter);
    }
    private void loadMessages() {
        // This method should fetch messages from your data source (e.g., database or API)
        // For demonstration, we will use dummy data
        // Replace this with actual data fetching logic
        MessageHistory message1 = new MessageHistory("John Doe", "Hello, how are you?", "10:00 AM", false);
        MessageHistory message2 = new MessageHistory("Jane Smith", "Don't forget our meeting tomorrow.", "10:05 AM", true);
        adapter.updateMessages(java.util.Arrays.asList(message1, message2));
    }
    @Override
    public void onMessageClick(MessageHistory message) {
        // Handle message click
        // For example, you can start a new activity to show message details
        Toast.makeText(this, "Clicked on: " + message.getSenderName(), Toast.LENGTH_SHORT).show();
    }
}