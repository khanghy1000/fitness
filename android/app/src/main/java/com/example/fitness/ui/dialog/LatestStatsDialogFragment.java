package com.example.fitness.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.fitness.R;
import com.example.fitness.databinding.DialogLatestStatsBinding;
import com.example.fitness.data.network.model.generated.LatestUserStats;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LatestStatsDialogFragment extends DialogFragment {
    
    private static final String ARG_WEIGHT = "weight";
    private static final String ARG_HEIGHT = "height";
    private static final String ARG_BODY_FAT = "body_fat";
    private static final String ARG_MUSCLE_MASS = "muscle_mass";
    private static final String ARG_CHEST = "chest";
    private static final String ARG_WAIST = "waist";
    private static final String ARG_HIPS = "hips";
    private static final String ARG_ARMS = "arms";
    private static final String ARG_THIGHS = "thighs";
    private static final String ARG_NOTES = "notes";
    private static final String ARG_RECORDED_AT = "recorded_at";
    
    private DialogLatestStatsBinding binding;
    
    // Individual fields
    private String weight;
    private String height;
    private String bodyFat;
    private String muscleMass;
    private String chest;
    private String waist;
    private String hips;
    private String arms;
    private String thighs;
    private String notes;
    private String recordedAt;
    
    public static LatestStatsDialogFragment newInstance(LatestUserStats latestStats) {
        LatestStatsDialogFragment fragment = new LatestStatsDialogFragment();
        Bundle args = new Bundle();
        
        // Convert BigDecimal values to strings for safe passing
        args.putString(ARG_WEIGHT, latestStats.getWeight() != null ? latestStats.getWeight().toString() : null);
        args.putString(ARG_HEIGHT, latestStats.getHeight() != null ? latestStats.getHeight().toString() : null);
        args.putString(ARG_BODY_FAT, latestStats.getBodyFat() != null ? latestStats.getBodyFat().toString() : null);
        args.putString(ARG_MUSCLE_MASS, latestStats.getMuscleMass() != null ? latestStats.getMuscleMass().toString() : null);
        args.putString(ARG_CHEST, latestStats.getChest() != null ? latestStats.getChest().toString() : null);
        args.putString(ARG_WAIST, latestStats.getWaist() != null ? latestStats.getWaist().toString() : null);
        args.putString(ARG_HIPS, latestStats.getHips() != null ? latestStats.getHips().toString() : null);
        args.putString(ARG_ARMS, latestStats.getArms() != null ? latestStats.getArms().toString() : null);
        args.putString(ARG_THIGHS, latestStats.getThighs() != null ? latestStats.getThighs().toString() : null);
        args.putString(ARG_NOTES, latestStats.getNotes());
        args.putString(ARG_RECORDED_AT, latestStats.getRecordedAt());
        
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            weight = getArguments().getString(ARG_WEIGHT);
            height = getArguments().getString(ARG_HEIGHT);
            bodyFat = getArguments().getString(ARG_BODY_FAT);
            muscleMass = getArguments().getString(ARG_MUSCLE_MASS);
            chest = getArguments().getString(ARG_CHEST);
            waist = getArguments().getString(ARG_WAIST);
            hips = getArguments().getString(ARG_HIPS);
            arms = getArguments().getString(ARG_ARMS);
            thighs = getArguments().getString(ARG_THIGHS);
            notes = getArguments().getString(ARG_NOTES);
            recordedAt = getArguments().getString(ARG_RECORDED_AT);
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogLatestStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViews();
        displayLatestStats();
    }
    
    private void setupViews() {
        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnCloseBottom.setOnClickListener(v -> dismiss());
    }
    
    private void displayLatestStats() {
        // Check if we have any data
        if (weight == null && height == null && bodyFat == null && muscleMass == null &&
            chest == null && waist == null && hips == null && arms == null && thighs == null) {
            binding.tvLatestStats.setText("No measurements recorded yet");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (weight != null) {
            sb.append("Weight: ").append(weight).append(" kg\n");
        }
        if (height != null) {
            sb.append("Height: ").append(height).append(" cm\n");
        }
        if (bodyFat != null) {
            sb.append("Body Fat: ").append(bodyFat).append("%\n");
        }
        if (muscleMass != null) {
            sb.append("Muscle Mass: ").append(muscleMass).append(" kg\n");
        }
        if (chest != null) {
            sb.append("Chest: ").append(chest).append(" cm\n");
        }
        if (waist != null) {
            sb.append("Waist: ").append(waist).append(" cm\n");
        }
        if (hips != null) {
            sb.append("Hips: ").append(hips).append(" cm\n");
        }
        if (arms != null) {
            sb.append("Arms: ").append(arms).append(" cm\n");
        }
        if (thighs != null) {
            sb.append("Thighs: ").append(thighs).append(" cm\n");
        }
        
        if (recordedAt != null) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            
            try {
                Date date = inputFormat.parse(recordedAt);
                if (date != null) {
                    sb.append("\nRecorded: ").append(outputFormat.format(date));
                }
            } catch (ParseException e) {
                sb.append("\nRecorded: ").append(recordedAt);
            }
        }
        
        if (notes != null && !notes.isEmpty()) {
            sb.append("\nNotes: ").append(notes);
        }

        binding.tvLatestStats.setText(sb.toString());
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
