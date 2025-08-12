package com.example.fitness.ui.fragment.trainee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitness.databinding.FragmentTraineeBodyStatsBinding;

public class TraineeBodyStatsFragment extends Fragment {
    private static final String ARG_TRAINEE_ID = "trainee_id";
    
    private FragmentTraineeBodyStatsBinding binding;
    private String traineeId;

    public static TraineeBodyStatsFragment newInstance(String traineeId) {
        TraineeBodyStatsFragment fragment = new TraineeBodyStatsFragment();
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
        binding = FragmentTraineeBodyStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
