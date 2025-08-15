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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.WorkoutPlan;
import com.example.fitness.data.network.model.generated.WorkoutPlanAssignment;
import com.example.fitness.databinding.FragmentWorkoutPlanListBinding;
import com.example.fitness.ui.activity.WorkoutPlanEditActivity;
import com.example.fitness.ui.activity.trainee.TraineeWorkoutPlanDetailsActivity;
import com.example.fitness.ui.adapter.TraineeWorkoutPlanAdapter;
import com.example.fitness.ui.viewmodel.WorkoutPlanViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WorkoutPlanListFragment extends Fragment implements TraineeWorkoutPlanAdapter.OnWorkoutPlanClickListener {

    private static final String ARG_STATUS = "status";
    
    public enum Status {
        ACTIVE, COMPLETED
    }

    private FragmentWorkoutPlanListBinding binding;
    private WorkoutPlanViewModel viewModel;
    private TraineeWorkoutPlanAdapter workoutPlanAdapter;
    private Status status;

    public static WorkoutPlanListFragment newInstance(Status status) {
        WorkoutPlanListFragment fragment = new WorkoutPlanListFragment();
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
        binding = FragmentWorkoutPlanListBinding.inflate(inflater, container, false);
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
        viewModel = new ViewModelProvider(requireActivity()).get(WorkoutPlanViewModel.class);
    }

    private void setupRecyclerView() {
        workoutPlanAdapter = new TraineeWorkoutPlanAdapter(this);
        binding.recyclerViewWorkoutPlans.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewWorkoutPlans.setAdapter(workoutPlanAdapter);
    }

    private void setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            if (status == Status.ACTIVE) {
                viewModel.refreshActiveWorkoutPlanAssignments();
            } else {
                viewModel.refreshCompletedWorkoutPlanAssignments();
            }
        });
    }

    private void observeViewModel() {
        if (status == Status.ACTIVE) {
            viewModel.activeWorkoutPlanAssignments.observe(getViewLifecycleOwner(), this::updateWorkoutPlans);
        } else {
            viewModel.completedWorkoutPlanAssignments.observe(getViewLifecycleOwner(), this::updateWorkoutPlans);
        }

        viewModel.creatorNames.observe(getViewLifecycleOwner(), creatorNames -> {
            if (creatorNames != null) {
                workoutPlanAdapter.updateCreatorNames(creatorNames);
            }
        });

        viewModel.isRefreshing.observe(getViewLifecycleOwner(), isRefreshing -> {
            binding.swipeRefreshLayout.setRefreshing(isRefreshing);
        });

        viewModel.currentUserId.observe(getViewLifecycleOwner(), currentUserId -> {
            if (currentUserId != null) {
                workoutPlanAdapter.setCurrentUserId(currentUserId);
            }
        });

        // Observe delete success/error messages
        viewModel.successMessage.observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null) {
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                viewModel.clearMessages();
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                viewModel.clearMessages();
            }
        });
    }

    private void updateWorkoutPlans(List<WorkoutPlanAssignment> workoutPlanAssignments) {
        if (workoutPlanAssignments != null && !workoutPlanAssignments.isEmpty()) {
            binding.textViewEmpty.setVisibility(View.GONE);
            binding.recyclerViewWorkoutPlans.setVisibility(View.VISIBLE);
            workoutPlanAdapter.updateWorkoutPlanAssignments(workoutPlanAssignments);
        } else {
            binding.textViewEmpty.setVisibility(View.VISIBLE);
            binding.recyclerViewWorkoutPlans.setVisibility(View.GONE);
            
            // Update empty message based on status
            if (status == Status.ACTIVE) {
                binding.textViewEmpty.setText("No active workout plans found");
            } else {
                binding.textViewEmpty.setText("No completed workout plans found");
            }
        }
    }

    @Override
    public void onWorkoutPlanClick(WorkoutPlan workoutPlan) {
        // Find the corresponding assignment to pass start date
        WorkoutPlanAssignment assignment = findAssignmentForPlan(workoutPlan);
        
        Intent intent = new Intent(getActivity(), TraineeWorkoutPlanDetailsActivity.class);
        intent.putExtra("PLAN_ID", workoutPlan.getId());
        intent.putExtra("PLAN_NAME", workoutPlan.getName());
        intent.putExtra("IS_COMPLETED", status == Status.COMPLETED);
        
        if (assignment != null) {
            intent.putExtra("ASSIGNMENT_ID", assignment.getId());
            intent.putExtra("START_DATE", assignment.getStartDate());
        }
        startActivity(intent);
    }

    @Override
    public void onWorkoutPlanEdit(WorkoutPlan workoutPlan) {
        Intent intent = new Intent(getActivity(), WorkoutPlanEditActivity.class);
        intent.putExtra("PLAN_ID", workoutPlan.getId());
        intent.putExtra("PLAN_NAME", workoutPlan.getName());
        startActivity(intent);
    }

    @Override
    public void onWorkoutPlanDelete(WorkoutPlan workoutPlan) {
        showDeleteConfirmationDialog(workoutPlan);
    }

    @Override
    public void onWorkoutPlanCancel(WorkoutPlanAssignment workoutPlanAssignment) {
        showCancelConfirmationDialog(workoutPlanAssignment);
    }

    @Override
    public void onWorkoutPlanOptionsClick(WorkoutPlan workoutPlan, View anchorView) {
        // Find the corresponding assignment to get assignment details
        WorkoutPlanAssignment assignment = findAssignmentForPlan(workoutPlan);
        
        PopupMenu popup = new PopupMenu(getContext(), anchorView);
        popup.getMenuInflater().inflate(R.menu.menu_workout_plan_options, popup.getMenu());
        
        // Check if current user created the plan
        String currentUserId = viewModel.currentUserId.getValue();
        boolean isCreatedByCurrentUser = workoutPlan.getCreatedBy() != null && 
            workoutPlan.getCreatedBy().equals(currentUserId);
        
        // Show cancel option only for active assignments
        popup.getMenu().findItem(R.id.action_cancel).setVisible(
            assignment != null && assignment.getStatus() == WorkoutPlanAssignment.Status.active
        );
        
        // Show edit/delete options only for plans created by current user
        popup.getMenu().findItem(R.id.action_edit).setVisible(isCreatedByCurrentUser);
        popup.getMenu().findItem(R.id.action_delete).setVisible(isCreatedByCurrentUser);
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_view_details) {
                onWorkoutPlanClick(workoutPlan);
                return true;
            } else if (itemId == R.id.action_cancel) {
                if (assignment != null) {
                    onWorkoutPlanCancel(assignment);
                }
                return true;
            } else if (itemId == R.id.action_edit) {
                onWorkoutPlanEdit(workoutPlan);
                return true;
            } else if (itemId == R.id.action_delete) {
                onWorkoutPlanDelete(workoutPlan);
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void showDeleteConfirmationDialog(WorkoutPlan workoutPlan) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Workout Plan")
                .setMessage("Are you sure you want to delete \"" + workoutPlan.getName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteWorkoutPlan(String.valueOf(workoutPlan.getId()));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCancelConfirmationDialog(WorkoutPlanAssignment workoutPlanAssignment) {
        String planName = workoutPlanAssignment.getWorkoutPlan().getName();
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Workout Plan")
                .setMessage("Are you sure you want to cancel \"" + planName + "\"? This will mark the plan as cancelled.")
                .setPositiveButton("Cancel Plan", (dialog, which) -> {
                    viewModel.cancelWorkoutPlan(String.valueOf(workoutPlanAssignment.getId()));
                })
                .setNegativeButton("Keep Plan", null)
                .show();
    }

    private WorkoutPlanAssignment findAssignmentForPlan(WorkoutPlan workoutPlan) {
        List<WorkoutPlanAssignment> assignments;
        if (status == Status.ACTIVE) {
            assignments = viewModel.activeWorkoutPlanAssignments.getValue();
        } else {
            assignments = viewModel.completedWorkoutPlanAssignments.getValue();
        }
        
        if (assignments != null) {
            for (WorkoutPlanAssignment assignment : assignments) {
                if (assignment.getWorkoutPlan().getId() == (workoutPlan.getId())) {
                    return assignment;
                }
            }
        }
        return null;
    }

    public void refreshData() {
        if (status == Status.ACTIVE) {
            viewModel.refreshActiveWorkoutPlanAssignments();
        } else {
            viewModel.refreshCompletedWorkoutPlanAssignments();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
