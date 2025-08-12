package com.example.fitness.ui.fragment.trainee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitness.data.network.model.generated.NutritionPlanAssignment;
import com.example.fitness.databinding.FragmentTraineeNutritionPlansBinding;
import com.example.fitness.ui.adapter.TraineeNutritionPlanAssignmentAdapter;
import com.example.fitness.ui.viewmodel.TraineeManagementViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TraineeNutritionPlansFragment extends Fragment {
    private static final String ARG_TRAINEE_ID = "trainee_id";
    
    private FragmentTraineeNutritionPlansBinding binding;
    private TraineeManagementViewModel viewModel;
    private TraineeNutritionPlanAssignmentAdapter adapter;
    private String traineeId;
    private String currentFilter = "Active";

    public static TraineeNutritionPlansFragment newInstance(String traineeId) {
        TraineeNutritionPlansFragment fragment = new TraineeNutritionPlansFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TRAINEE_ID, traineeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            traineeId = getArguments().getString(ARG_TRAINEE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTraineeNutritionPlansBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(TraineeManagementViewModel.class);
        viewModel.setTraineeId(traineeId);
        
        setupRecyclerView();
        setupSwipeRefresh();
        setupStatusFilter();
        observeViewModel();
        loadData();
    }

    private void setupRecyclerView() {
        adapter = new TraineeNutritionPlanAssignmentAdapter(null);
        binding.rvNutritionPlans.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNutritionPlans.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadData();
        });
    }

    private void setupStatusFilter() {
        String[] statusOptions = {"All", "Active", "Completed"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            statusOptions
        );
        
        binding.spinnerStatusFilter.setAdapter(statusAdapter);
        binding.spinnerStatusFilter.setOnItemClickListener((parent, view, position, id) -> {
            currentFilter = statusOptions[position];
            adapter.filterByStatus(currentFilter);
            updateEmptyState();
        });
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() > 0) {
            binding.rvNutritionPlans.setVisibility(View.VISIBLE);
            binding.tvEmptyState.setVisibility(View.GONE);
        } else {
            binding.rvNutritionPlans.setVisibility(View.GONE);
            binding.tvEmptyState.setVisibility(View.VISIBLE);
        }
    }

    private void observeViewModel() {
        viewModel.traineeNutritionPlans.observe(getViewLifecycleOwner(), nutritionPlans -> {
            adapter.updateAssignments(nutritionPlans);
            adapter.filterByStatus(currentFilter);
            updateEmptyState();
            binding.swipeRefresh.setRefreshing(false);
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (!binding.swipeRefresh.isRefreshing()) {
                // Only show progress bar if not already showing swipe refresh
                // You can add a progress bar to the layout if needed
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearMessages();
            }
            binding.swipeRefresh.setRefreshing(false);
        });

        viewModel.successMessage.observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
                viewModel.clearMessages();
            }
        });
    }

    private void loadData() {
        viewModel.loadTraineeNutritionPlans();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
