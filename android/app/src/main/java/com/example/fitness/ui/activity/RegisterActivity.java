package com.example.fitness.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitness.R;
import com.example.fitness.databinding.ActivityRegisterBinding;
import com.example.fitness.ui.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String role = binding.rbCoach.isChecked() ? "coach" : "trainee";
            
            authViewModel.signUp(name, email, password, role);
        });

        binding.tvLoginLink.setOnClickListener(v -> {
            finish(); // Go back to login activity
        });
    }

    private void observeViewModel() {
        authViewModel.isLoading.observe(this, isLoading -> {
            if (isLoading) {
                binding.btnRegister.setEnabled(false);
                binding.btnRegister.setText("");
                binding.progressBar.setVisibility(View.VISIBLE);
            } else {
                binding.btnRegister.setEnabled(true);
                binding.btnRegister.setText("CREATE ACCOUNT");
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        authViewModel.errorMessage.observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                binding.tvError.setText(errorMessage);
                binding.tvError.setVisibility(View.VISIBLE);
            } else {
                binding.tvError.setVisibility(View.GONE);
            }
        });

        authViewModel.registerSuccess.observe(this, success -> {
            if (success) {
                // Clear any error messages
                authViewModel.clearError();
                
                // Navigate to MainActivity
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                
                // Reset the success state
                authViewModel.resetRegisterSuccess();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
