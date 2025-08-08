package com.example.fitness.ble;

import org.json.JSONObject;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles exercise rep counting based on BLE predictions
 * Similar to the Python count_rep_3.py implementation
 */
public class BleExerciseCounter {
    
    public interface RepCountListener {
        void onRepCompleted(int repCount);
        void onExerciseStateChanged(boolean isDoingExercise, String exerciseName, double confidence);
        void onDebugLog(String logMessage);
    }
    
    private final String targetExercise;
    private int repCount;
    private boolean isDoingExercise;
    private long lastRepTime;
    private final long minRepGap = 1000; // 1 second in milliseconds
    private final double exerciseThreshold = 0.4;
    private final ArrayDeque<Boolean> stateBuffer;
    private RepCountListener listener;
    
    public BleExerciseCounter(String targetExercise) {
        this.targetExercise = targetExercise.toLowerCase();
        this.repCount = 0;
        this.isDoingExercise = false;
        this.lastRepTime = 0;
        this.stateBuffer = new ArrayDeque<>(3);
    }
    
    public void setRepCountListener(RepCountListener listener) {
        this.listener = listener;
        
        // Log initialization now that listener is set
        if (listener != null) {
            listener.onDebugLog("🎯 Counting ONLY: " + this.targetExercise.toUpperCase());
            listener.onDebugLog("⚙️ Detection threshold: " + exerciseThreshold);
        }
    }
    
    /**
     * Process prediction data from BLE device
     */
    public void processPrediction(JSONObject predictionsJson) {
        try {
            long currentTime = System.currentTimeMillis();
            
            // Get confidence for target exercise
            double targetConfidence = predictionsJson.optDouble(targetExercise, 0.0);
            
            // Binary decision: doing target exercise or not
            boolean isTargetExercise = targetConfidence >= exerciseThreshold;
            
            // Add to buffer for stability (keep last 3 readings)
            if (stateBuffer.size() >= 3) {
                stateBuffer.removeFirst();
            }
            stateBuffer.addLast(isTargetExercise);
            
            // Need at least 2 readings for stability
            if (stateBuffer.size() < 2) {
                return;
            }
            
            // Check for state change with majority vote
            int trueCount = 0;
            for (Boolean state : stateBuffer) {
                if (state) trueCount++;
            }
            boolean currentMajority = trueCount >= 2; // At least 2 out of 3 are true
            
            // State transitions
            if (!isDoingExercise && currentMajority) {
                // Started doing the exercise
                if (currentTime - lastRepTime >= minRepGap) {
                    isDoingExercise = true;
                    if (listener != null) {
                        String startMessage = "🟢 Started " + targetExercise + "... (confidence: " + 
                            String.format("%.2f", targetConfidence) + ")";
                        listener.onDebugLog(startMessage);
                        listener.onExerciseStateChanged(true, targetExercise, targetConfidence);
                    }
                }
            } else if (isDoingExercise && !currentMajority) {
                // Finished the exercise = 1 rep completed
                isDoingExercise = false;
                repCount++;
                lastRepTime = currentTime;
                
                if (listener != null) {
                    String repMessage = "✅ REP #" + repCount + " COMPLETED!";
                    String totalMessage = "📊 Total " + targetExercise + ": " + repCount + " reps";
                    String separator = "------------------------------";
                    
                    listener.onRepCompleted(repCount);
                    listener.onDebugLog(totalMessage);
                    listener.onDebugLog(separator);
                    listener.onExerciseStateChanged(false, targetExercise, targetConfidence);
                }
            }
            
            // Update status (like Python script's continuous status display)
            if (listener != null) {
                String emoji = isDoingExercise ? "🟢 DOING" : "⚪ READY";
                String confidenceDisplay = String.format("%.2f", targetConfidence);
                
                // Create buffer display like Python script
                StringBuilder bufferDisplay = new StringBuilder("[");
                int count = 0;
                for (Boolean state : stateBuffer) {
                    if (count > 0) bufferDisplay.append(", ");
                    bufferDisplay.append(state);
                    count++;
                }
                bufferDisplay.append("]");
                
                String statusLine = emoji + " | " + targetExercise + ": " + confidenceDisplay + 
                    " | Buffer: " + bufferDisplay.toString();
                
                listener.onDebugLog(statusLine);
            }
            
        } catch (Exception e) {
            if (listener != null) {
                String errorMessage = "❌ Error processing prediction: " + e.getMessage();
                listener.onDebugLog(errorMessage);
            }
        }
    }
    
    public void reset() {
        repCount = 0;
        isDoingExercise = false;
        lastRepTime = 0;
        stateBuffer.clear();
        if (listener != null) {
            String resetMessage = "🔄 " + targetExercise.toUpperCase() + " counter reset to 0!";
            listener.onDebugLog(resetMessage);
        }
    }
    
    public int getRepCount() {
        return repCount;
    }
    
    public String getTargetExercise() {
        return targetExercise;
    }
    
    public boolean isDoingExercise() {
        return isDoingExercise;
    }
    
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("exercise", targetExercise.toUpperCase());
        summary.put("total_reps", repCount);
        summary.put("session_time", System.currentTimeMillis());
        return summary;
    }
}
