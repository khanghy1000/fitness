package com.example.fitness.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitness.data.network.model.generated.ExerciseType;
import com.example.fitness.databinding.DialogEditWorkoutExerciseInfoBinding;
import com.example.fitness.ui.viewmodel.WorkoutPlanEditViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditWorkoutExerciseInfoDialogFragment extends DialogFragment {
    
    public interface OnWorkoutExerciseInfoEditListener {
        void onWorkoutExerciseInfoEdited(int exerciseTypeId, String exerciseTypeName, ExerciseType.LogType logType, Integer targetReps, Integer targetDuration, String notes);
    }
    
    private static final String ARG_EXERCISE_TYPE_ID = "exercise_type_id";
    private static final String ARG_EXERCISE_TYPE_NAME = "exercise_type_name";
    private static final String ARG_TARGET_REPS = "target_reps";
    private static final String ARG_TARGET_DURATION = "target_duration";
    private static final String ARG_NOTES = "notes";
    
    private DialogEditWorkoutExerciseInfoBinding binding;
    private OnWorkoutExerciseInfoEditListener listener;
    private WorkoutPlanEditViewModel viewModel;
    private List<ExerciseType> exerciseTypes = new ArrayList<>();
    
    public static EditWorkoutExerciseInfoDialogFragment newInstance(int exerciseTypeId, String exerciseTypeName, Integer targetReps, Integer targetDuration, String notes) {
        EditWorkoutExerciseInfoDialogFragment fragment = new EditWorkoutExerciseInfoDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_EXERCISE_TYPE_ID, exerciseTypeId);
        args.putString(ARG_EXERCISE_TYPE_NAME, exerciseTypeName);
        if (targetReps != null) args.putInt(ARG_TARGET_REPS, targetReps);
        if (targetDuration != null) args.putInt(ARG_TARGET_DURATION, targetDuration);
        args.putString(ARG_NOTES, notes);
        fragment.setArguments(args);
        return fragment;
    }
    
    public void setOnWorkoutExerciseInfoEditListener(OnWorkoutExerciseInfoEditListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogEditWorkoutExerciseInfoBinding.inflate(LayoutInflater.from(getContext()));
        viewModel = new ViewModelProvider(requireActivity()).get(WorkoutPlanEditViewModel.class);
        
        // Observe exercise types
        viewModel.exerciseTypes.observe(this, types -> {
            if (types != null) {
                exerciseTypes.clear();
                exerciseTypes.addAll(types);
                setupExerciseTypeSpinner();
                updateInputFieldsBasedOnLogType();
            }
        });
        
        // Get arguments and populate fields
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_TARGET_REPS)) {
                binding.editTextTargetReps.setText(String.valueOf(args.getInt(ARG_TARGET_REPS)));
            }
            if (args.containsKey(ARG_TARGET_DURATION)) {
                binding.editTextTargetDuration.setText(String.valueOf(args.getInt(ARG_TARGET_DURATION)));
            }
            binding.editTextNotes.setText(args.getString(ARG_NOTES, ""));
        }
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Exercise Information")
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
    
    private void setupExerciseTypeSpinner() {
        List<String> exerciseNames = new ArrayList<>();
        for (ExerciseType type : exerciseTypes) {
            exerciseNames.add(type.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
            android.R.layout.simple_dropdown_item_1line, exerciseNames);
        binding.spinnerExerciseType.setAdapter(adapter);
        
        // Set current exercise type
        Bundle args = getArguments();
        if (args != null) {
            String currentExerciseName = args.getString(ARG_EXERCISE_TYPE_NAME);
            if (currentExerciseName != null) {
                binding.spinnerExerciseType.setText(currentExerciseName, false);
            }
        }
        
        // Add listener to update input fields when exercise type changes
        binding.spinnerExerciseType.setOnItemClickListener((parent, view, position, id) -> {
            updateInputFieldsBasedOnLogType();
        });
    }
    
    private void updateInputFieldsBasedOnLogType() {
        String exerciseTypeName = binding.spinnerExerciseType.getText().toString().trim();
        ExerciseType selectedType = null;
        
        for (ExerciseType type : exerciseTypes) {
            if (type.getName().equals(exerciseTypeName)) {
                selectedType = type;
                break;
            }
        }
        
        if (selectedType != null) {
            if (selectedType.getLogType() == ExerciseType.LogType.reps) {
                // Show reps field, hide duration field
                binding.layoutTargetReps.setVisibility(android.view.View.VISIBLE);
                binding.layoutTargetDuration.setVisibility(android.view.View.GONE);
                // Clear duration field if it was previously filled
                binding.editTextTargetDuration.setText("");
            } else if (selectedType.getLogType() == ExerciseType.LogType.duration) {
                // Show duration field, hide reps field
                binding.layoutTargetReps.setVisibility(android.view.View.GONE);
                binding.layoutTargetDuration.setVisibility(android.view.View.VISIBLE);
                // Clear reps field if it was previously filled
                binding.editTextTargetReps.setText("");
            }
        } else {
            // If no exercise type selected, hide both
            binding.layoutTargetReps.setVisibility(android.view.View.GONE);
            binding.layoutTargetDuration.setVisibility(android.view.View.GONE);
        }
    }
    
    private boolean validateAndSave() {
        String exerciseTypeName = binding.spinnerExerciseType.getText().toString().trim();
        String targetRepsText = binding.editTextTargetReps.getText() != null ? 
            binding.editTextTargetReps.getText().toString().trim() : "";
        String targetDurationText = binding.editTextTargetDuration.getText() != null ? 
            binding.editTextTargetDuration.getText().toString().trim() : "";
        String notes = binding.editTextNotes.getText() != null ? 
            binding.editTextNotes.getText().toString().trim() : "";
        
        if (exerciseTypeName.isEmpty()) {
            binding.spinnerExerciseType.setError("Exercise type is required");
            return false;
        }
        
        // Find exercise type ID and LogType
        int exerciseTypeId = -1;
        ExerciseType.LogType logType = null;
        for (ExerciseType type : exerciseTypes) {
            if (type.getName().equals(exerciseTypeName)) {
                exerciseTypeId = type.getId();
                logType = type.getLogType();
                break;
            }
        }
        
        if (exerciseTypeId == -1) {
            binding.spinnerExerciseType.setError("Invalid exercise type");
            return false;
        }
        
        Integer targetReps = null;
        Integer targetDuration = null;
        
        try {
            if (logType == ExerciseType.LogType.reps) {
                // For reps exercises, targetReps is required
                if (targetRepsText.isEmpty()) {
                    binding.editTextTargetReps.setError("Target reps is required for this exercise");
                    return false;
                }
                targetReps = Integer.parseInt(targetRepsText);
                if (targetReps <= 0) {
                    binding.editTextTargetReps.setError("Target reps must be positive");
                    return false;
                }
            } else if (logType == ExerciseType.LogType.duration) {
                // For duration exercises, targetDuration is required
                if (targetDurationText.isEmpty()) {
                    binding.editTextTargetDuration.setError("Target duration is required for this exercise");
                    return false;
                }
                targetDuration = Integer.parseInt(targetDurationText);
                if (targetDuration <= 0) {
                    binding.editTextTargetDuration.setError("Target duration must be positive");
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            if (logType == ExerciseType.LogType.reps) {
                binding.editTextTargetReps.setError("Invalid number format");
            } else {
                binding.editTextTargetDuration.setError("Invalid number format");
            }
            return false;
        }
        
        if (listener != null) {
            listener.onWorkoutExerciseInfoEdited(exerciseTypeId, exerciseTypeName, logType, targetReps, targetDuration, notes.isEmpty() ? null : notes);
        }
        
        return true;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
