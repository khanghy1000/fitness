package com.example.fitness.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitness.databinding.FragmentHomeMoreBinding;
import com.example.fitness.ui.activity.LoginActivity;
import com.example.fitness.ui.viewmodel.MainViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeMoreFragment extends Fragment {
    
    private FragmentHomeMoreBinding binding;
    private MainViewModel mainViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeMoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get the shared ViewModel from the parent activity
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        
        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.btnLogout.setOnClickListener(v -> {
            mainViewModel.logout();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    private void observeViewModel() {
        // Observe user info
        mainViewModel.userInfo.observe(getViewLifecycleOwner(), userInfo -> {
            if (userInfo != null) {
                binding.tvUserInfo.setText(userInfo);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
