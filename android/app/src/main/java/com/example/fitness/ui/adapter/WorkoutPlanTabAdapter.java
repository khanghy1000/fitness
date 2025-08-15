package com.example.fitness.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fitness.ui.fragment.WorkoutPlanListFragment;

import java.util.HashMap;
import java.util.Map;

public class WorkoutPlanTabAdapter extends FragmentStateAdapter {

    private Map<Integer, WorkoutPlanListFragment> fragmentMap = new HashMap<>();

    public WorkoutPlanTabAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        WorkoutPlanListFragment fragment;
        switch (position) {
            case 0:
                fragment = WorkoutPlanListFragment.newInstance(WorkoutPlanListFragment.Status.ACTIVE);
                break;
            case 1:
                fragment = WorkoutPlanListFragment.newInstance(WorkoutPlanListFragment.Status.COMPLETED);
                break;
            default:
                fragment = WorkoutPlanListFragment.newInstance(WorkoutPlanListFragment.Status.ACTIVE);
                break;
        }
        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2; // Active and Completed tabs
    }

    public void refreshFragments() {
        for (WorkoutPlanListFragment fragment : fragmentMap.values()) {
            if (fragment != null && fragment.isAdded()) {
                fragment.refreshData();
            }
        }
    }
}
