package com.example.fitness.utils;

public class DurationUtil {
    
    /**
     * Formats duration in seconds to a readable string format
     * @param durationInSeconds Duration in seconds
     * @return Formatted string (e.g., "1 min 30 sec", "0 min 20 sec", "2 min")
     */
    public static String formatDuration(int durationInSeconds) {
        if (durationInSeconds <= 0) {
            return "0 min";
        }
        
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        
        if (minutes > 0 && seconds > 0) {
            return minutes + " min " + seconds + " sec";
        } else if (minutes > 0) {
            return minutes + " min";
        } else {
            return "0 min " + seconds + " sec";
        }
    }
}
