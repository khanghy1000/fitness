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

import com.example.fitness.data.network.model.generated.BulkUpdateWorkoutPlan;
import com.example.fitness.databinding.DialogEditWorkoutPlanInfoBinding;

public class EditWorkoutPlanInfoDialogFragment extends DialogFragment {
    
    public interface OnWorkoutPlanInfoEditListener {
        void onWorkoutPlanInfoEdited(String name, String description, BulkUpdateWorkoutPlan.Difficulty difficulty, boolean isActive);
    }
    
    private static final String ARG_NAME = "name";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_DIFFICULTY = "difficulty";
    private static final String ARG_IS_ACTIVE = "is_active";
    
    private DialogEditWorkoutPlanInfoBinding binding;
    private OnWorkoutPlanInfoEditListener listener;
    
    public static EditWorkoutPlanInfoDialogFragment newInstance(String name, String description, BulkUpdateWorkoutPlan.Difficulty difficulty, boolean isActive) {
        EditWorkoutPlanInfoDialogFragment fragment = new EditWorkoutPlanInfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_DESCRIPTION, description);
        if (difficulty != null) {
            args.putString(ARG_DIFFICULTY, difficulty.getValue());
        }
        args.putBoolean(ARG_IS_ACTIVE, isActive);
        fragment.setArguments(args);
        return fragment;
    }
    
    public void setOnWorkoutPlanInfoEditListener(OnWorkoutPlanInfoEditListener listener) {
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
        
        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            binding.editTextPlanName.setText(args.getString(ARG_NAME));
            binding.editTextPlanDescription.setText(args.getString(ARG_DESCRIPTION));

            String difficulty = args.getString(ARG_DIFFICULTY);
            if (difficulty != null) {
                String capitalizedDifficulty = difficulty.substring(0, 1).toUpperCase() + difficulty.substring(1);
                binding.spinnerDifficulty.setText(capitalizedDifficulty, false);
            } else {
                binding.spinnerDifficulty.setText(difficulties[0], false);
            }
        }
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Workout Plan Information")
                .setView(binding.getRoot())
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (d, which) -> dismiss())
                .create();
        
        // Override positive button to validate input
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validateAndSave()) {
                    dismiss();
                }
            });
        });
        
        return dialog;
    }
    
    private boolean validateAndSave() {
        String name = binding.editTextPlanName.getText() != null ? 
            binding.editTextPlanName.getText().toString().trim() : "";
        String description = binding.editTextPlanDescription.getText() != null ? 
            binding.editTextPlanDescription.getText().toString().trim() : "";
        String difficultyText = binding.spinnerDifficulty.getText().toString();
        
        if (name.isEmpty()) {
            binding.editTextPlanName.setError("Plan name is required");
            return false;
        }
        
        BulkUpdateWorkoutPlan.Difficulty difficulty;
        switch (difficultyText.toLowerCase()) {
            case "beginner":
                difficulty = BulkUpdateWorkoutPlan.Difficulty.beginner;
                break;
            case "intermediate":
                difficulty = BulkUpdateWorkoutPlan.Difficulty.intermediate;
                break;
            case "advanced":
                difficulty = BulkUpdateWorkoutPlan.Difficulty.advanced;
                break;
            default:
                difficulty = BulkUpdateWorkoutPlan.Difficulty.beginner;
                break;
        }
        
        if (listener != null) {
            listener.onWorkoutPlanInfoEdited(name, description.isEmpty() ? null : description, difficulty, true);
        }
        
        return true;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
