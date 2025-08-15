package com.example.fitness.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.fitness.databinding.DialogCreateNutritionPlanBinding;

public class CreateNutritionPlanDialogFragment extends DialogFragment {
    
    public interface OnNutritionPlanCreateListener {
        void onNutritionPlanCreated(String name, String description);
    }
    
    private DialogCreateNutritionPlanBinding binding;
    private OnNutritionPlanCreateListener listener;
    
    public void setOnNutritionPlanCreateListener(OnNutritionPlanCreateListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogCreateNutritionPlanBinding.inflate(LayoutInflater.from(getContext()));
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Create Nutrition Plan")
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
        
        if (name.isEmpty()) {
            binding.editTextPlanName.setError("Plan name is required");
            return false;
        }
        
        if (listener != null) {
            listener.onNutritionPlanCreated(name, description.isEmpty() ? null : description);
        }
        
        return true;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
