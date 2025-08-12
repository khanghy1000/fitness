package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.WorkoutPlan;

import java.util.List;

public class WorkoutPlanSelectionAdapter extends BaseAdapter {
    private List<WorkoutPlan> workoutPlans;
    private OnWorkoutPlanSelectedListener listener;

    public interface OnWorkoutPlanSelectedListener {
        void onWorkoutPlanSelected(WorkoutPlan workoutPlan);
    }

    public WorkoutPlanSelectionAdapter(List<WorkoutPlan> workoutPlans, OnWorkoutPlanSelectedListener listener) {
        this.workoutPlans = workoutPlans;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return workoutPlans.size();
    }

    @Override
    public Object getItem(int position) {
        return workoutPlans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return workoutPlans.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        WorkoutPlan plan = workoutPlans.get(position);
        
        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);
        
        text1.setText(plan.getName());
        text2.setText(plan.getDescription());
        
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWorkoutPlanSelected(plan);
            }
        });
        
        return convertView;
    }
}
