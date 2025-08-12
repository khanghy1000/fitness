package com.example.fitness.ui.activity.trainee;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitness.R;
import com.example.fitness.databinding.ActivityTraineeStatsBinding;
import com.example.fitness.ui.viewmodel.TraineeStatsViewModel;
import com.example.fitness.data.network.model.generated.UserStats;
import com.example.fitness.data.network.model.generated.LatestUserStats;
import com.example.fitness.ui.dialog.LatestStatsDialogFragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TraineeStatsActivity extends AppCompatActivity {

    private ActivityTraineeStatsBinding binding;
    private TraineeStatsViewModel viewModel;
    private List<UserStats> allStats = new ArrayList<>();
    private String currentChartType = "weight";
    private LatestUserStats latestStatsData;
    
    // Chart type options
    private final String[] chartTypes = {
        "weight", "height", "bodyFat", "muscleMass", 
        "chest", "waist", "hips", "arms", "thighs"
    };
    
    private final String[] chartLabels = {
        "Weight", "Height", "Body Fat", "Muscle Mass",
        "Chest", "Waist", "Hips", "Arms", "Thighs"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityTraineeStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(TraineeStatsViewModel.class);
        
        setupViews();
        observeViewModel();
        loadData();
    }

    private void setupViews() {
        setupStatsDropdown();
        setupChart();
        
        // FAB click to show form
        binding.fabAdd.setOnClickListener(v -> showRecordForm());
        
        // Close form button
        binding.btnCloseForm.setOnClickListener(v -> hideRecordForm());
        
        // Overlay background click to close form
        binding.overlayBackground.setOnClickListener(v -> hideRecordForm());
        
        // Record button click
        binding.btnRecord.setOnClickListener(v -> recordStats());
        
        // View latest stats button
        binding.btnViewLatestStats.setOnClickListener(v -> showLatestStatsDialog());
    }

    private void setupStatsDropdown() {
        MaterialAutoCompleteTextView spinner = binding.spinnerStatsType;
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_dropdown_item_1line, 
            chartLabels
        );
        
        spinner.setAdapter(adapter);
        spinner.setText(chartLabels[0], false); // Set default to Weight
        
        spinner.setOnItemClickListener((parent, view, position, id) -> {
            currentChartType = chartTypes[position];
            updateChart();
        });
    }

    private void setupChart() {
        LineChart chart = binding.chart;
        
        // Configure chart appearance
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        
        // Description
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        
        // X-axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(6);
        
        // Y-axis
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setAxisMinimum(0f);
        
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
        
        // Legend
        chart.getLegend().setEnabled(true);
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRecord.setEnabled(!isLoading);
        });

        viewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearMessages();
            }
        });

        viewModel.recordSuccess.observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Stats recorded successfully!", Toast.LENGTH_SHORT).show();
                clearInputFields();
                viewModel.clearMessages();
            }
        });

        viewModel.userStats.observe(this, stats -> {
            if (stats != null) {
                allStats = stats;
                updateChart();
            }
        });

        viewModel.latestStats.observe(this, latestStats -> {
            latestStatsData = latestStats;
            updateLatestStatsButtonText();
        });
    }

    private void loadData() {
        viewModel.loadUserStats();
        viewModel.loadLatestStats();
    }

    private void recordStats() {
        try {
            Double weight = parseDouble(binding.etWeight.getText().toString());
            Double height = parseDouble(binding.etHeight.getText().toString());
            Double bodyFat = parseDouble(binding.etBodyFat.getText().toString());
            Double muscleMass = parseDouble(binding.etMuscleMass.getText().toString());
            Double chest = parseDouble(binding.etChest.getText().toString());
            Double waist = parseDouble(binding.etWaist.getText().toString());
            Double hips = parseDouble(binding.etHips.getText().toString());
            Double arms = parseDouble(binding.etArms.getText().toString());
            Double thighs = parseDouble(binding.etThighs.getText().toString());
            String notes = binding.etNotes.getText().toString().trim();
            
            if (notes.isEmpty()) {
                notes = null;
            }

            viewModel.recordStats(weight, height, bodyFat, muscleMass, chest, waist, hips, arms, thighs, notes);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private Double parseDouble(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void clearInputFields() {
        binding.etWeight.setText("");
        binding.etHeight.setText("");
        binding.etBodyFat.setText("");
        binding.etMuscleMass.setText("");
        binding.etChest.setText("");
        binding.etWaist.setText("");
        binding.etHips.setText("");
        binding.etArms.setText("");
        binding.etThighs.setText("");
        binding.etNotes.setText("");
        hideRecordForm();
    }

    private void updateLatestStatsButtonText() {
        if (latestStatsData != null) {
            binding.btnViewLatestStats.setText("View Latest Measurements");
        } else {
            binding.btnViewLatestStats.setText("No Measurements Yet");
        }
    }
    
    private void showLatestStatsDialog() {
        if (latestStatsData != null) {
            LatestStatsDialogFragment dialog = LatestStatsDialogFragment.newInstance(latestStatsData);
            dialog.show(getSupportFragmentManager(), "LatestStatsDialog");
        } else {
            Toast.makeText(this, "No measurements recorded yet", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateChart() {
        if (allStats == null || allStats.isEmpty()) {
            binding.chart.clear();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        
        // Sort stats by date
        List<UserStats> sortedStats = new ArrayList<>(allStats);
        Collections.sort(sortedStats, (a, b) -> {
            try {
                Date dateA = inputFormat.parse(a.getRecordedAt());
                Date dateB = inputFormat.parse(b.getRecordedAt());
                return dateA.compareTo(dateB);
            } catch (ParseException e) {
                return 0;
            }
        });

        int index = 0;
        for (UserStats stat : sortedStats) {
            BigDecimal value = null;
            
            switch (currentChartType) {
                case "weight":
                    value = stat.getWeight();
                    break;
                case "height":
                    value = stat.getHeight();
                    break;
                case "bodyFat":
                    value = stat.getBodyFat();
                    break;
                case "muscleMass":
                    value = stat.getMuscleMass();
                    break;
                case "chest":
                    value = stat.getChest();
                    break;
                case "waist":
                    value = stat.getWaist();
                    break;
                case "hips":
                    value = stat.getHips();
                    break;
                case "arms":
                    value = stat.getArms();
                    break;
                case "thighs":
                    value = stat.getThighs();
                    break;
            }
            
            if (value != null) {
                entries.add(new Entry(index, value.floatValue()));
                
                try {
                    Date date = inputFormat.parse(stat.getRecordedAt());
                    if (date != null) {
                        labels.add(outputFormat.format(date));
                    } else {
                        labels.add("Date " + (index + 1));
                    }
                } catch (ParseException e) {
                    labels.add("Date " + (index + 1));
                }
                index++;
            }
        }

        if (entries.isEmpty()) {
            binding.chart.clear();
            return;
        }

        String label;
        int color;
        switch (currentChartType) {
            case "weight":
                label = "Weight (kg)";
                color = Color.BLUE;
                break;
            case "height":
                label = "Height (cm)";
                color = Color.GREEN;
                break;
            case "bodyFat":
                label = "Body Fat (%)";
                color = Color.RED;
                break;
            case "muscleMass":
                label = "Muscle Mass (kg)";
                color = Color.MAGENTA;
                break;
            case "chest":
                label = "Chest (cm)";
                color = Color.CYAN;
                break;
            case "waist":
                label = "Waist (cm)";
                color = Color.parseColor("#FF9800"); // Orange
                break;
            case "hips":
                label = "Hips (cm)";
                color = Color.parseColor("#9C27B0"); // Purple
                break;
            case "arms":
                label = "Arms (cm)";
                color = Color.parseColor("#4CAF50"); // Green
                break;
            case "thighs":
                label = "Thighs (cm)";
                color = Color.parseColor("#795548"); // Brown
                break;
            default:
                label = "Value";
                color = Color.GRAY;
                break;
        }

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(false);

        LineData lineData = new LineData(dataSet);
        binding.chart.setData(lineData);
        
        // Update X-axis labels
        XAxis xAxis = binding.chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(Math.min(labels.size(), 6));
        
        binding.chart.invalidate(); // refresh
    }

    private void showRecordForm() {
        binding.overlayBackground.setVisibility(View.VISIBLE);
        binding.cardRecordStats.setVisibility(View.VISIBLE);
    }
    
    private void hideRecordForm() {
        binding.overlayBackground.setVisibility(View.GONE);
        binding.cardRecordStats.setVisibility(View.GONE);
    }
    


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}