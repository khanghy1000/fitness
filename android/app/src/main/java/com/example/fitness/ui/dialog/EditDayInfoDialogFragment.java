package com.example.fitness.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.fitness.databinding.DialogEditDayInfoBinding;

import java.util.ArrayList;
import java.util.List;

public class EditDayInfoDialogFragment extends DialogFragment {
    
    public interface OnDayInfoEditListener {
        void onDayInfoEdited(String weekday, String calories, String protein, String carbs, String fat);
    }
    
    private static final String ARG_WEEKDAY = "weekday";
    private static final String ARG_CALORIES = "calories";
    private static final String ARG_PROTEIN = "protein";
    private static final String ARG_CARBS = "carbs";
    private static final String ARG_FAT = "fat";
    private static final String ARG_AVAILABLE_WEEKDAYS = "available_weekdays";
    
    private DialogEditDayInfoBinding binding;
    private OnDayInfoEditListener listener;
    
    public static EditDayInfoDialogFragment newInstance(String weekday, String calories, String protein, 
                                                       String carbs, String fat, ArrayList<String> availableWeekdays) {
        EditDayInfoDialogFragment fragment = new EditDayInfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_WEEKDAY, weekday);
        args.putString(ARG_CALORIES, calories);
        args.putString(ARG_PROTEIN, protein);
        args.putString(ARG_CARBS, carbs);
        args.putString(ARG_FAT, fat);
        args.putStringArrayList(ARG_AVAILABLE_WEEKDAYS, availableWeekdays);
        fragment.setArguments(args);
        return fragment;
    }
    
    public void setOnDayInfoEditListener(OnDayInfoEditListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogEditDayInfoBinding.inflate(LayoutInflater.from(getContext()));
        
        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            String currentWeekday = args.getString(ARG_WEEKDAY);
            List<String> availableWeekdays = args.getStringArrayList(ARG_AVAILABLE_WEEKDAYS);
            
            // Setup weekday dropdown
            if (availableWeekdays != null) {
                // Add current weekday to available list if it's already set
                if (currentWeekday != null && !currentWeekday.isEmpty() && !availableWeekdays.contains(currentWeekday)) {
                    availableWeekdays.add(currentWeekday);
                }
                
                // Convert weekday codes to display names
                List<String> displayWeekdays = new ArrayList<>();
                for (String weekday : availableWeekdays) {
                    displayWeekdays.add(capitalizeWeekday(weekday));
                }
                
                ArrayAdapter<String> weekdayAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, displayWeekdays);
                binding.autoCompleteWeekday.setAdapter(weekdayAdapter);
                binding.autoCompleteWeekday.setText(capitalizeWeekday(currentWeekday), false);
            }
            
            binding.editTextCalories.setText(args.getString(ARG_CALORIES));
            binding.editTextProtein.setText(args.getString(ARG_PROTEIN));
            binding.editTextCarbs.setText(args.getString(ARG_CARBS));
            binding.editTextFat.setText(args.getString(ARG_FAT));
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
        String selectedDisplayName = binding.autoCompleteWeekday.getText() != null ? 
            binding.autoCompleteWeekday.getText().toString().trim() : "";
        String calories = binding.editTextCalories.getText() != null ? 
            binding.editTextCalories.getText().toString().trim() : "";
        String protein = binding.editTextProtein.getText() != null ? 
            binding.editTextProtein.getText().toString().trim() : "";
        String carbs = binding.editTextCarbs.getText() != null ? 
            binding.editTextCarbs.getText().toString().trim() : "";
        String fat = binding.editTextFat.getText() != null ? 
            binding.editTextFat.getText().toString().trim() : "";
        
        // Validate weekday selection
        if (selectedDisplayName.isEmpty()) {
            binding.textInputLayoutWeekday.setError("Please select a day of the week");
            return false;
        }
        
        binding.textInputLayoutWeekday.setError(null);
        
        String weekdayCode = convertDisplayNameToCode(selectedDisplayName);
        
        if (listener != null) {
            listener.onDayInfoEdited(weekdayCode, calories, protein, carbs, fat);
        }
        
        return true;
    }
    
    private String convertDisplayNameToCode(String displayName) {
        switch (displayName) {
            case "Sunday": return "sun";
            case "Monday": return "mon";
            case "Tuesday": return "tue";
            case "Wednesday": return "wed";
            case "Thursday": return "thu";
            case "Friday": return "fri";
            case "Saturday": return "sat";
            default: return displayName.toLowerCase();
        }
    }

    private String capitalizeWeekday(String weekday) {
        if (weekday == null || weekday.isEmpty()) return "";
        
        switch (weekday.toLowerCase()) {
            case "sun": return "Sunday";
            case "mon": return "Monday";
            case "tue": return "Tuesday";
            case "wed": return "Wednesday";
            case "thu": return "Thursday";
            case "fri": return "Friday";
            case "sat": return "Saturday";
            default: return weekday;
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
