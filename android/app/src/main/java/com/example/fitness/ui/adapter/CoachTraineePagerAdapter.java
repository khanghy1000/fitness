package com.example.fitness.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fitness.ui.fragment.ConnectionsListFragment;

public class CoachTraineePagerAdapter extends FragmentStateAdapter {

    public CoachTraineePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return ConnectionsListFragment.newInstance(ConnectionAdapter.ConnectionType.ACTIVE_CONNECTION);
            case 1:
                return ConnectionsListFragment.newInstance(ConnectionAdapter.ConnectionType.RECEIVED_REQUEST);
            default:
                return ConnectionsListFragment.newInstance(ConnectionAdapter.ConnectionType.ACTIVE_CONNECTION);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
