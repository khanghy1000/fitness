package com.example.fitness.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.fitness.databinding.DialogEditWorkoutDayInfoBinding;

public class EditWorkoutDayInfoDialogFragment extends DialogFragment {
    
    public interface OnWorkoutDayInfoEditListener {
        void onWorkoutDayInfoEdited(int dayNumber, boolean isRestDay);
    }
    
    private static final String ARG_DAY_NUMBER = "day_number";
    private static final String ARG_IS_REST_DAY = "is_rest_day";
    
    private DialogEditWorkoutDayInfoBinding binding;
    private OnWorkoutDayInfoEditListener listener;
    
    public static EditWorkoutDayInfoDialogFragment newInstance(int dayNumber, boolean isRestDay) {
        EditWorkoutDayInfoDialogFragment fragment = new EditWorkoutDayInfoDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DAY_NUMBER, dayNumber);
        args.putBoolean(ARG_IS_REST_DAY, isRestDay);
        fragment.setArguments(args);
        return fragment;
    }
    
    public void setOnWorkoutDayInfoEditListener(OnWorkoutDayInfoEditListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogEditWorkoutDayInfoBinding.inflate(LayoutInflater.from(getContext()));
        
        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            binding.editTextDayNumber.setText(String.valueOf(args.getInt(ARG_DAY_NUMBER)));
            binding.switchIsRestDay.setChecked(args.getBoolean(ARG_IS_REST_DAY));
        }
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Day Information")
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
        String dayNumberText = binding.editTextDayNumber.getText() != null ? 
            binding.editTextDayNumber.getText().toString().trim() : "";
        boolean isRestDay = binding.switchIsRestDay.isChecked();
        
        if (dayNumberText.isEmpty()) {
            binding.editTextDayNumber.setError("Day number is required");
            return false;
        }
        
        int dayNumber;
        try {
            dayNumber = Integer.parseInt(dayNumberText);
            if (dayNumber < 1 || dayNumber > 7) {
                binding.editTextDayNumber.setError("Day number must be between 1 and 7");
                return false;
            }
        } catch (NumberFormatException e) {
            binding.editTextDayNumber.setError("Invalid day number");
            return false;
        }
        
        if (listener != null) {
            listener.onWorkoutDayInfoEdited(dayNumber, isRestDay);
        }
        
        return true;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
