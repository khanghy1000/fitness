package com.example.fitness.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fitness.ui.fragment.WorkoutPlanListFragment;

public class WorkoutPlanTabAdapter extends FragmentStateAdapter {

    public WorkoutPlanTabAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return WorkoutPlanListFragment.newInstance(WorkoutPlanListFragment.Status.ACTIVE);
            case 1:
                return WorkoutPlanListFragment.newInstance(WorkoutPlanListFragment.Status.COMPLETED);
            default:
                return WorkoutPlanListFragment.newInstance(WorkoutPlanListFragment.Status.ACTIVE);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Active and Completed tabs
    }
}
