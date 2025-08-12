package com.example.fitness.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fitness.ui.fragment.trainee.TraineeWorkoutPlansFragment;
import com.example.fitness.ui.fragment.trainee.TraineeNutritionPlansFragment;
import com.example.fitness.ui.fragment.trainee.TraineeBodyStatsFragment;

public class TraineeManagementPagerAdapter extends FragmentStateAdapter {
    private String traineeId;

    public TraineeManagementPagerAdapter(@NonNull FragmentActivity fragmentActivity, String traineeId) {
        super(fragmentActivity);
        this.traineeId = traineeId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return TraineeWorkoutPlansFragment.newInstance(traineeId);
            case 1:
                return TraineeNutritionPlansFragment.newInstance(traineeId);
            case 2:
                return TraineeBodyStatsFragment.newInstance(traineeId);
            default:
                return TraineeWorkoutPlansFragment.newInstance(traineeId);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
