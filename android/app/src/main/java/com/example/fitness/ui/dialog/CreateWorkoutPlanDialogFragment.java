package com.example.fitness.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.fitness.data.network.model.generated.CreateWorkoutPlan;
import com.example.fitness.databinding.DialogEditWorkoutPlanInfoBinding;

public class CreateWorkoutPlanDialogFragment extends DialogFragment {
    
    public interface OnWorkoutPlanCreateListener {
        void onWorkoutPlanCreated(String name, String description, CreateWorkoutPlan.Difficulty difficulty);
    }
    
    private DialogEditWorkoutPlanInfoBinding binding;
    private OnWorkoutPlanCreateListener listener;
    
    public void setOnWorkoutPlanCreateListener(OnWorkoutPlanCreateListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogEditWorkoutPlanInfoBinding.inflate(LayoutInflater.from(getContext()));
        
        // Setup difficulty spinner
        String[] difficulties = {"Beginner", "Intermediate", "Advanced"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
            android.R.layout.simple_dropdown_item_1line, difficulties);
        binding.spinnerDifficulty.setAdapter(adapter);
        binding.spinnerDifficulty.setText(difficulties[0], false);
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Create Workout Plan")
                .setView(binding.getRoot())
                .setPositiveButton("Create", null)
                .setNegativeButton("Cancel", (d, which) -> dismiss())
                .create();
        
        // Override positive button to validate input
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validateAndCreate()) {
                    dismiss();
                }
            });
        });
        
        return dialog;
    }
    
    private boolean validateAndCreate() {
        String name = binding.editTextPlanName.getText() != null ? 
            binding.editTextPlanName.getText().toString().trim() : "";
        String description = binding.editTextPlanDescription.getText() != null ? 
            binding.editTextPlanDescription.getText().toString().trim() : "";
        String difficultyText = binding.spinnerDifficulty.getText().toString();
        
        if (name.isEmpty()) {
            binding.editTextPlanName.setError("Plan name is required");
            return false;
        }
        
        CreateWorkoutPlan.Difficulty difficulty;
        switch (difficultyText.toLowerCase()) {
            case "beginner":
                difficulty = CreateWorkoutPlan.Difficulty.beginner;
                break;
            case "intermediate":
                difficulty = CreateWorkoutPlan.Difficulty.intermediate;
                break;
            case "advanced":
                difficulty = CreateWorkoutPlan.Difficulty.advanced;
                break;
            default:
                difficulty = CreateWorkoutPlan.Difficulty.beginner;
                break;
        }
        
        if (listener != null) {
            listener.onWorkoutPlanCreated(name, description.isEmpty() ? null : description, difficulty);
        }
        
        return true;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
