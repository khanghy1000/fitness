package com.example.fitness.ui.fragment.trainee;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitness.databinding.FragmentTraineeBodyStatsBinding;
import com.example.fitness.ui.viewmodel.CoachTraineeStatsViewModel;
import com.example.fitness.data.network.model.generated.UserStats;
import com.example.fitness.data.network.model.generated.LatestUserStats;
import com.example.fitness.ui.dialog.LatestStatsDialogFragment;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

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
public class TraineeBodyStatsFragment extends Fragment {
    private static final String ARG_TRAINEE_ID = "trainee_id";
    
    private FragmentTraineeBodyStatsBinding binding;
    private CoachTraineeStatsViewModel viewModel;
    private String traineeId;
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

    public static TraineeBodyStatsFragment newInstance(String traineeId) {
        TraineeBodyStatsFragment fragment = new TraineeBodyStatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TRAINEE_ID, traineeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            traineeId = getArguments().getString(ARG_TRAINEE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTraineeBodyStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(CoachTraineeStatsViewModel.class);
        
        setupViews();
        observeViewModel();
        
        if (traineeId != null) {
            viewModel.setTraineeId(traineeId);
        }
    }

    private void setupViews() {
        setupStatsDropdown();
        setupChart();
        
        // View latest stats button
        binding.btnViewLatestStats.setOnClickListener(v -> showLatestStatsDialog());
    }

    private void setupStatsDropdown() {
        MaterialAutoCompleteTextView spinner = binding.spinnerStatsType;
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(), 
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
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (binding != null) {
                binding.progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearMessages();
            }
        });

        viewModel.userStats.observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                allStats = stats;
                updateChart();
                updateNoDataVisibility();
            }
        });

        viewModel.latestStats.observe(getViewLifecycleOwner(), latestStats -> {
            latestStatsData = latestStats;
            updateLatestStatsButtonText();
        });
    }

    private void updateLatestStatsButtonText() {
        if (binding != null) {
            if (latestStatsData != null) {
                binding.btnViewLatestStats.setText("View Latest Measurements");
            } else {
                binding.btnViewLatestStats.setText("No Measurements Yet");
            }
        }
    }
    
    private void showLatestStatsDialog() {
        if (latestStatsData != null) {
            LatestStatsDialogFragment dialog = LatestStatsDialogFragment.newInstance(latestStatsData);
            dialog.show(getParentFragmentManager(), "LatestStatsDialog");
        } else {
            Toast.makeText(requireContext(), "No measurements recorded yet", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNoDataVisibility() {
        if (binding != null) {
            boolean hasData = allStats != null && !allStats.isEmpty();
            binding.tvNoData.setVisibility(hasData ? View.GONE : View.VISIBLE);
            binding.cardCharts.setVisibility(hasData ? View.VISIBLE : View.GONE);
        }
    }

    private void updateChart() {
        if (binding == null || allStats == null || allStats.isEmpty()) {
            if (binding != null) {
                binding.chart.clear();
            }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}