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
import androidx.recyclerview.widget.LinearLayoutManager;

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
