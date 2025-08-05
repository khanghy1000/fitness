package com.example.fitness.ui.activity.trainee;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitness.R;

public class TraineeWorkoutActivity extends AppCompatActivity {

    private static final String TAG = "TraineeWorkoutActivity";
    private String planId;
    private String planName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trainee_workout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get plan information from intent
        if (getIntent() != null) {
            planId = getIntent().getStringExtra("PLAN_ID");
            planName = getIntent().getStringExtra("PLAN_NAME");
            
            Log.d(TAG, "Plan ID: " + planId);
            Log.d(TAG, "Plan Name: " + planName);
            
            // TODO: Implement workout plan details and exercise display
        }
    }
}