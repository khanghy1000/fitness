package com.example.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.NutritionPlan;

import java.util.List;

public class NutritionPlanSelectionAdapter extends BaseAdapter {
    private List<NutritionPlan> nutritionPlans;
    private OnNutritionPlanSelectedListener listener;

    public interface OnNutritionPlanSelectedListener {
        void onNutritionPlanSelected(NutritionPlan nutritionPlan);
    }

    public NutritionPlanSelectionAdapter(List<NutritionPlan> nutritionPlans, OnNutritionPlanSelectedListener listener) {
        this.nutritionPlans = nutritionPlans;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return nutritionPlans.size();
    }

    @Override
    public Object getItem(int position) {
        return nutritionPlans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return nutritionPlans.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        NutritionPlan plan = nutritionPlans.get(position);
        
        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);
        
        text1.setText(plan.getName());
        text2.setText(plan.getDescription());
        
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNutritionPlanSelected(plan);
            }
        });
        
        return convertView;
    }
}
