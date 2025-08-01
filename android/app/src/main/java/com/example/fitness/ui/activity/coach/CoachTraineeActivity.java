package com.example.fitness.ui.activity.coach;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitness.R;
import com.example.fitness.databinding.ActivityCoachTraineeBinding;
import com.example.fitness.ui.adapter.CoachTraineePagerAdapter;
import com.example.fitness.ui.viewmodel.ConnectionsViewModel;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CoachTraineeActivity extends AppCompatActivity {
    
    private ActivityCoachTraineeBinding binding;
    private ConnectionsViewModel connectionsViewModel;
    private CoachTraineePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityCoachTraineeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        setupToolbar();
        setupViewPager();
        setupViewModel();
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupViewPager() {
        pagerAdapter = new CoachTraineePagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);
        
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Active Trainees");
                    break;
                case 1:
                    tab.setText("Requests");
                    break;
            }
        }).attach();
    }
    
    private void setupViewModel() {
        connectionsViewModel = new ViewModelProvider(this).get(ConnectionsViewModel.class);
        
        connectionsViewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        });
        
        connectionsViewModel.errorMessage.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                connectionsViewModel.clearMessages();
            }
        });
        
        connectionsViewModel.successMessage.observe(this, success -> {
            if (success != null) {
                Toast.makeText(this, success, Toast.LENGTH_SHORT).show();
                connectionsViewModel.clearMessages();
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}