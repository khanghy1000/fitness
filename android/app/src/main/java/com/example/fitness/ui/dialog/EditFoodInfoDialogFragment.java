package com.example.fitness.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.fitness.databinding.DialogEditFoodInfoBinding;

public class EditFoodInfoDialogFragment extends DialogFragment {
    
    public interface OnFoodInfoEditListener {
        void onFoodInfoEdited(String name, String quantity, String calories);
    }
    
    private static final String ARG_NAME = "name";
    private static final String ARG_QUANTITY = "quantity";
    private static final String ARG_CALORIES = "calories";
    
    private DialogEditFoodInfoBinding binding;
    private OnFoodInfoEditListener listener;
    
    public static EditFoodInfoDialogFragment newInstance(String name, String quantity, String calories) {
        EditFoodInfoDialogFragment fragment = new EditFoodInfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_QUANTITY, quantity);
        args.putString(ARG_CALORIES, calories);
        fragment.setArguments(args);
        return fragment;
    }
    
    public void setOnFoodInfoEditListener(OnFoodInfoEditListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogEditFoodInfoBinding.inflate(LayoutInflater.from(getContext()));
        
        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            binding.editTextFoodName.setText(args.getString(ARG_NAME));
            binding.editTextFoodQuantity.setText(args.getString(ARG_QUANTITY));
            binding.editTextFoodCalories.setText(args.getString(ARG_CALORIES));
        }
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Food Information")
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
        String name = binding.editTextFoodName.getText() != null ? 
            binding.editTextFoodName.getText().toString().trim() : "";
        String quantity = binding.editTextFoodQuantity.getText() != null ? 
            binding.editTextFoodQuantity.getText().toString().trim() : "";
        String calories = binding.editTextFoodCalories.getText() != null ? 
            binding.editTextFoodCalories.getText().toString().trim() : "";
        
        // Validate required fields
        boolean hasErrors = false;
        
        if (name.isEmpty()) {
            binding.textInputLayoutFoodName.setError("Food name is required");
            hasErrors = true;
        } else {
            binding.textInputLayoutFoodName.setError(null);
        }
        
        if (quantity.isEmpty()) {
            binding.textInputLayoutFoodQuantity.setError("Quantity is required");
            hasErrors = true;
        } else {
            binding.textInputLayoutFoodQuantity.setError(null);
        }
        
        if (calories.isEmpty()) {
            binding.textInputLayoutFoodCalories.setError("Calories is required");
            hasErrors = true;
        } else {
            binding.textInputLayoutFoodCalories.setError(null);
        }
        
        if (hasErrors) {
            return false;
        }
        
        if (listener != null) {
            listener.onFoodInfoEdited(name, quantity, calories);
        }
        
        return true;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
