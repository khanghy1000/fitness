package com.example.fitness.ui.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.fitness.databinding.DialogEditMealInfoBinding;

import java.util.Calendar;

public class EditMealInfoDialogFragment extends DialogFragment {
    
    public interface OnMealInfoEditListener {
        void onMealInfoEdited(String name, String time, String calories, String protein, String carbs, String fat);
    }
    
    private static final String ARG_NAME = "name";
    private static final String ARG_TIME = "time";
    private static final String ARG_CALORIES = "calories";
    private static final String ARG_PROTEIN = "protein";
    private static final String ARG_CARBS = "carbs";
    private static final String ARG_FAT = "fat";
    
    private DialogEditMealInfoBinding binding;
    private OnMealInfoEditListener listener;
    
    public static EditMealInfoDialogFragment newInstance(String name, String time, String calories, 
                                                        String protein, String carbs, String fat) {
        EditMealInfoDialogFragment fragment = new EditMealInfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_TIME, time);
        args.putString(ARG_CALORIES, calories);
        args.putString(ARG_PROTEIN, protein);
        args.putString(ARG_CARBS, carbs);
        args.putString(ARG_FAT, fat);
        fragment.setArguments(args);
        return fragment;
    }
    
    public void setOnMealInfoEditListener(OnMealInfoEditListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogEditMealInfoBinding.inflate(LayoutInflater.from(getContext()));
        
        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            binding.editTextMealName.setText(args.getString(ARG_NAME));
            binding.editTextMealTime.setText(args.getString(ARG_TIME));
            binding.editTextMealCalories.setText(args.getString(ARG_CALORIES));
            binding.editTextMealProtein.setText(args.getString(ARG_PROTEIN));
            binding.editTextMealCarbs.setText(args.getString(ARG_CARBS));
            binding.editTextMealFat.setText(args.getString(ARG_FAT));
        }
        
        // Setup time picker for meal time
        binding.editTextMealTime.setFocusable(false);
        binding.editTextMealTime.setOnClickListener(v -> showTimePicker());
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Meal Information")
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
    
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = 0;
        int minute = 0;
        
        String current = binding.editTextMealTime.getText() != null ? 
            binding.editTextMealTime.getText().toString() : "";
        if (current.matches("\\d{2}:\\d{2}:\\d{2}")) {
            hour = Integer.parseInt(current.substring(0, 2));
            minute = Integer.parseInt(current.substring(3, 5));
        }
        
        new TimePickerDialog(getContext(), (view, selectedHour, selectedMinute) -> {
            String formatted = String.format("%02d:%02d:00", selectedHour, selectedMinute);
            binding.editTextMealTime.setText(formatted);
        }, hour, minute, true).show();
    }
    
    private boolean validateAndSave() {
        String name = binding.editTextMealName.getText() != null ? 
            binding.editTextMealName.getText().toString().trim() : "";
        String time = binding.editTextMealTime.getText() != null ? 
            binding.editTextMealTime.getText().toString().trim() : "";
        String calories = binding.editTextMealCalories.getText() != null ? 
            binding.editTextMealCalories.getText().toString().trim() : "";
        String protein = binding.editTextMealProtein.getText() != null ? 
            binding.editTextMealProtein.getText().toString().trim() : "";
        String carbs = binding.editTextMealCarbs.getText() != null ? 
            binding.editTextMealCarbs.getText().toString().trim() : "";
        String fat = binding.editTextMealFat.getText() != null ? 
            binding.editTextMealFat.getText().toString().trim() : "";
        
        // Validate required fields
        boolean hasErrors = false;
        
        if (name.isEmpty()) {
            binding.textInputLayoutMealName.setError("Meal name is required");
            hasErrors = true;
        } else {
            binding.textInputLayoutMealName.setError(null);
        }
        
        if (time.isEmpty()) {
            binding.textInputLayoutMealTime.setError("Meal time is required");
            hasErrors = true;
        } else {
            binding.textInputLayoutMealTime.setError(null);
        }
        
        if (hasErrors) {
            return false;
        }
        
        if (listener != null) {
            listener.onMealInfoEdited(name, time, calories, protein, carbs, fat);
        }
        
        return true;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
