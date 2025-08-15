package com.example.fitness.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.NutritionPlanAssignment;
import com.example.fitness.databinding.FragmentNutritionPlanListBinding;
import com.example.fitness.ui.activity.NutritionPlanEditActivity;
import com.example.fitness.ui.activity.trainee.TraineeNutritionPlanDetailsActivity;
import com.example.fitness.ui.adapter.TraineeNutritionPlanAssignmentAdapter;
import com.example.fitness.ui.viewmodel.TraineeNutritionPlanViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NutritionPlanListFragment extends Fragment implements TraineeNutritionPlanAssignmentAdapter.OnAssignmentClickListener {

    private static final String ARG_STATUS = "status";
    
    public enum Status {
        ACTIVE, COMPLETED
    }

    private FragmentNutritionPlanListBinding binding;
    private TraineeNutritionPlanViewModel viewModel;
    private TraineeNutritionPlanAssignmentAdapter adapter;
    private Status status;

    public static NutritionPlanListFragment newInstance(Status status) {
        NutritionPlanListFragment fragment = new NutritionPlanListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            status = (Status) getArguments().getSerializable(ARG_STATUS);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNutritionPlanListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(TraineeNutritionPlanViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new TraineeNutritionPlanAssignmentAdapter(this);
        binding.recyclerViewNutritionPlans.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewNutritionPlans.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshNutritionPlans();
        });
    }

    private void observeViewModel() {
        viewModel.nutritionPlanAssignments.observe(getViewLifecycleOwner(), assignments -> {
            if (assignments != null) {
                filterAndUpdateAssignments(assignments);
            }
        });

        viewModel.creatorNames.observe(getViewLifecycleOwner(), creatorNames -> {
            if (creatorNames != null) {
                adapter.updateCreatorNames(creatorNames);
            }
        });

        viewModel.isRefreshing.observe(getViewLifecycleOwner(), isRefreshing -> {
            binding.swipeRefreshLayout.setRefreshing(isRefreshing);
        });

        viewModel.currentUserId.observe(getViewLifecycleOwner(), currentUserId -> {
            if (currentUserId != null) {
                adapter.setCurrentUserId(currentUserId);
            }
        });

        viewModel.successMessage.observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null) {
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                viewModel.clearSuccessMessage();
            }
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
    }

    private void filterAndUpdateAssignments(List<NutritionPlanAssignment> allAssignments) {
        List<NutritionPlanAssignment> filteredAssignments = allAssignments.stream()
                .filter(assignment -> {
                    if (status == Status.ACTIVE) {
                        return assignment.getStatus() == NutritionPlanAssignment.Status.active;
                    } else {
                        return assignment.getStatus() == NutritionPlanAssignment.Status.completed ||
                               assignment.getStatus() == NutritionPlanAssignment.Status.cancelled;
                    }
                })
                .collect(java.util.stream.Collectors.toList());

        adapter.updateAssignments(filteredAssignments);
        updateEmptyState(filteredAssignments.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.textViewEmpty.setVisibility(View.VISIBLE);
            binding.recyclerViewNutritionPlans.setVisibility(View.GONE);
            
            // Update empty message based on status
            if (status == Status.ACTIVE) {
                binding.textViewEmpty.setText("No active nutrition plans found");
            } else {
                binding.textViewEmpty.setText("No completed nutrition plans found");
            }
        } else {
            binding.textViewEmpty.setVisibility(View.GONE);
            binding.recyclerViewNutritionPlans.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAssignmentClick(NutritionPlanAssignment assignment) {
        Intent intent = new Intent(getActivity(), TraineeNutritionPlanDetailsActivity.class);
        intent.putExtra("ASSIGNMENT_ID", assignment.getId());
        intent.putExtra("PLAN_ID", assignment.getNutritionPlanId());
        intent.putExtra("PLAN_NAME", assignment.getNutritionPlan() != null ? 
            assignment.getNutritionPlan().getName() : "Nutrition Plan #" + assignment.getNutritionPlanId());
        startActivity(intent);
    }

    @Override
    public void onAssignmentEdit(NutritionPlanAssignment assignment) {
        Intent intent = new Intent(getActivity(), NutritionPlanEditActivity.class);
        intent.putExtra("PLAN_ID", assignment.getNutritionPlanId());
        intent.putExtra("PLAN_NAME", assignment.getNutritionPlan() != null ? 
            assignment.getNutritionPlan().getName() : "Nutrition Plan #" + assignment.getNutritionPlanId());
        startActivity(intent);
    }

    @Override
    public void onAssignmentOptions(NutritionPlanAssignment assignment, View anchorView) {
        PopupMenu popup = new PopupMenu(getContext(), anchorView);
        popup.getMenuInflater().inflate(R.menu.menu_nutrition_plan_options, popup.getMenu());
        
        // Check if current user created the plan
        String currentUserId = viewModel.currentUserId.getValue();
        boolean isCreatedByCurrentUser = assignment.getNutritionPlan() != null && 
            assignment.getNutritionPlan().getCreatedBy() != null && 
            assignment.getNutritionPlan().getCreatedBy().equals(currentUserId);
        
        // Show complete option only for active assignments
        popup.getMenu().findItem(R.id.action_complete).setVisible(
            assignment.getStatus() == NutritionPlanAssignment.Status.active
        );
        
        // Show edit/delete options only for plans created by current user
        popup.getMenu().findItem(R.id.action_edit).setVisible(isCreatedByCurrentUser);
        popup.getMenu().findItem(R.id.action_delete).setVisible(isCreatedByCurrentUser);
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_complete) {
                showCompleteConfirmationDialog(assignment);
                return true;
            } else if (itemId == R.id.action_edit) {
                onAssignmentEdit(assignment);
                return true;
            } else if (itemId == R.id.action_delete) {
                showDeleteConfirmationDialog(assignment);
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void showCompleteConfirmationDialog(NutritionPlanAssignment assignment) {
        String planName = assignment.getNutritionPlan() != null ? 
            assignment.getNutritionPlan().getName() : "Nutrition Plan #" + assignment.getNutritionPlanId();
            
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Complete Nutrition Plan")
                .setMessage("Are you sure you want to mark \"" + planName + "\" as completed?")
                .setPositiveButton("Complete", (dialog, which) -> {
                    viewModel.completeNutritionPlan(String.valueOf(assignment.getId()));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmationDialog(NutritionPlanAssignment assignment) {
        String planName = assignment.getNutritionPlan() != null ? 
            assignment.getNutritionPlan().getName() : "Nutrition Plan #" + assignment.getNutritionPlanId();
            
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Nutrition Plan")
                .setMessage("Are you sure you want to delete \"" + planName + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteNutritionPlan(String.valueOf(assignment.getNutritionPlanId()));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void refreshData() {
        viewModel.refreshNutritionPlans();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
