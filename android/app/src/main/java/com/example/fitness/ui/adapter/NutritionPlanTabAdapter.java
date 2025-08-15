package com.example.fitness.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fitness.ui.fragment.NutritionPlanListFragment;

import java.util.HashMap;
import java.util.Map;

public class NutritionPlanTabAdapter extends FragmentStateAdapter {

    private Map<Integer, NutritionPlanListFragment> fragmentMap = new HashMap<>();

    public NutritionPlanTabAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        NutritionPlanListFragment fragment;
        switch (position) {
            case 0:
                fragment = NutritionPlanListFragment.newInstance(NutritionPlanListFragment.Status.ACTIVE);
                break;
            case 1:
                fragment = NutritionPlanListFragment.newInstance(NutritionPlanListFragment.Status.COMPLETED);
                break;
            default:
                fragment = NutritionPlanListFragment.newInstance(NutritionPlanListFragment.Status.ACTIVE);
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
        for (NutritionPlanListFragment fragment : fragmentMap.values()) {
            if (fragment != null && fragment.isAdded()) {
                fragment.refreshData();
            }
        }
    }
}
