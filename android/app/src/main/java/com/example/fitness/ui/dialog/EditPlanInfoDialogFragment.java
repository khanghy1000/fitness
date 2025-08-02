package com.example.fitness.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.fitness.databinding.DialogEditPlanInfoBinding;

public class EditPlanInfoDialogFragment extends DialogFragment {
    
    public interface OnPlanInfoEditListener {
        void onPlanInfoEdited(String name, String description, boolean isActive);
    }
    
    private static final String ARG_NAME = "name";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_IS_ACTIVE = "is_active";
    
    private DialogEditPlanInfoBinding binding;
    private OnPlanInfoEditListener listener;
    
    public static EditPlanInfoDialogFragment newInstance(String name, String description, boolean isActive) {
        EditPlanInfoDialogFragment fragment = new EditPlanInfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_DESCRIPTION, description);
        args.putBoolean(ARG_IS_ACTIVE, isActive);
        fragment.setArguments(args);
        return fragment;
    }
    
    public void setOnPlanInfoEditListener(OnPlanInfoEditListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogEditPlanInfoBinding.inflate(LayoutInflater.from(getContext()));
        
        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            binding.editTextPlanName.setText(args.getString(ARG_NAME));
            binding.editTextPlanDescription.setText(args.getString(ARG_DESCRIPTION));
            binding.switchIsActive.setChecked(args.getBoolean(ARG_IS_ACTIVE));
        }
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Plan Information")
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
        boolean isActive = binding.switchIsActive.isChecked();
        
        // Validate plan name
        if (name.isEmpty()) {
            binding.textInputLayoutPlanName.setError("Plan name is required");
            return false;
        }
        
        binding.textInputLayoutPlanName.setError(null);
        
        if (listener != null) {
            listener.onPlanInfoEdited(name, description.isEmpty() ? null : description, isActive);
        }
        
        return true;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
