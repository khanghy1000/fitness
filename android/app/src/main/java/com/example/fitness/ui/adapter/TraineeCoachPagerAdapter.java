package com.example.fitness.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fitness.ui.fragment.CoachSearchFragment;
import com.example.fitness.ui.fragment.ConnectionsListFragment;

public class TraineeCoachPagerAdapter extends FragmentStateAdapter {

    public TraineeCoachPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return ConnectionsListFragment.newInstance(ConnectionAdapter.ConnectionType.ACTIVE_CONNECTION);
            case 1:
                return CoachSearchFragment.newInstance();
            case 2:
                return ConnectionsListFragment.newInstance(ConnectionAdapter.ConnectionType.SENT_REQUEST);
            default:
                return ConnectionsListFragment.newInstance(ConnectionAdapter.ConnectionType.ACTIVE_CONNECTION);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
