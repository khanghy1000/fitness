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
        void onFoodInfoEdited(String name, String quantity, String calories, String protein, String carbs, String fat, String fiber);
    }
    
    private static final String ARG_NAME = "name";
    private static final String ARG_QUANTITY = "quantity";
    private static final String ARG_CALORIES = "calories";
    private static final String ARG_PROTEIN = "protein";
    private static final String ARG_CARBS = "carbs";
    private static final String ARG_FAT = "fat";
    private static final String ARG_FIBER = "fiber";
    
    private DialogEditFoodInfoBinding binding;
    private OnFoodInfoEditListener listener;
    
    public static EditFoodInfoDialogFragment newInstance(String name, String quantity, String calories, 
                                                        String protein, String carbs, String fat, String fiber) {
        EditFoodInfoDialogFragment fragment = new EditFoodInfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_QUANTITY, quantity);
        args.putString(ARG_CALORIES, calories);
        args.putString(ARG_PROTEIN, protein);
        args.putString(ARG_CARBS, carbs);
        args.putString(ARG_FAT, fat);
        args.putString(ARG_FIBER, fiber);
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
            binding.editTextFoodProtein.setText(args.getString(ARG_PROTEIN));
            binding.editTextFoodCarbs.setText(args.getString(ARG_CARBS));
            binding.editTextFoodFat.setText(args.getString(ARG_FAT));
            binding.editTextFoodFiber.setText(args.getString(ARG_FIBER));
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
        String protein = binding.editTextFoodProtein.getText() != null ? 
            binding.editTextFoodProtein.getText().toString().trim() : "";
        String carbs = binding.editTextFoodCarbs.getText() != null ? 
            binding.editTextFoodCarbs.getText().toString().trim() : "";
        String fat = binding.editTextFoodFat.getText() != null ? 
            binding.editTextFoodFat.getText().toString().trim() : "";
        String fiber = binding.editTextFoodFiber.getText() != null ? 
            binding.editTextFoodFiber.getText().toString().trim() : "";
        
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
            listener.onFoodInfoEdited(name, quantity, calories, protein, carbs, fat, fiber);
        }
        
        return true;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
