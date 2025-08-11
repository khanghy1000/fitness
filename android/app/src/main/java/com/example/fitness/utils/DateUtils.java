package com.example.fitness.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    private static final SimpleDateFormat API_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    /**
     * Calculate the current day of a workout plan based on start date
     * @param startDateStr Start date of the workout plan assignment
     * @return Current day number (1-based), or -1 if start date is in the future
     */
    public static int getCurrentWorkoutDay(String startDateStr) {
        try {
            Date startDate = parseDate(startDateStr);
            Date today = getCurrentDate();
            
            // Normalize start date to midnight for accurate day comparison
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            Date normalizedStartDate = startCal.getTime();
            
            long diffInMillis = today.getTime() - normalizedStartDate.getTime();
            
            // If start date is in the future, return -1
            if (diffInMillis < 0) {
                return -1;
            }
            
            // Calculate days difference and add 1 for 1-based indexing
            long daysDiff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
            int currentDay = (int) daysDiff + 1;
            
            return currentDay;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Check if a specific workout day is available for recording results
     * @param startDateStr Start date of the workout plan assignment
     * @param dayNumber Day number to check (1-based)
     * @return true if day is current or past, false if future
     */
    public static boolean isDayAvailableForRecording(String startDateStr, int dayNumber) {
        int currentDay = getCurrentWorkoutDay(startDateStr);
        return currentDay >= dayNumber;
    }
    
    /**
     * Check if a specific workout day is the current day
     * @param startDateStr Start date of the workout plan assignment
     * @param dayNumber Day number to check (1-based)
     * @return true if this is the current day
     */
    public static boolean isCurrentDay(String startDateStr, int dayNumber) {
        int currentDay = getCurrentWorkoutDay(startDateStr);
        boolean isCurrent = currentDay == dayNumber;
        
        android.util.Log.d("DateUtils", "isCurrentDay - dayNumber: " + dayNumber + ", currentDay: " + currentDay + ", result: " + isCurrent);
        
        return isCurrent;
    }
    
    /**
     * Get the date for a specific workout day
     * @param startDateStr Start date of the workout plan assignment
     * @param dayNumber Day number (1-based)
     * @return Date for the specified day
     */
    public static Date getDateForWorkoutDay(String startDateStr, int dayNumber) {
        try {
            Date startDate = parseDate(startDateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_YEAR, dayNumber - 1);
            return calendar.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Parse date string from API (handles multiple formats)
     * @param dateStr Date string to parse
     * @return Parsed Date object
     * @throws ParseException if parsing fails
     */
    private static Date parseDate(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.isEmpty()) {
            throw new ParseException("Date string is null or empty", 0);
        }
        
        try {
            // Try full ISO format first
            return API_DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            // Try simple date format as fallback
            return SIMPLE_DATE_FORMAT.parse(dateStr);
        }
    }
    
    /**
     * Get current date with time set to midnight for day comparison
     * @return Current date at midnight
     */
    private static Date getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    
    /**
     * Format date for display
     * @param date Date to format
     * @return Formatted date string
     */
    public static String formatDisplayDate(Date date) {
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return displayFormat.format(date);
    }
}
