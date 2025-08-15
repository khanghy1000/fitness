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
    
    // Maximum reasonable workout plan duration in days
    private static final int MAX_WORKOUT_PLAN_DAYS = 365;
    // Maximum reasonable time range for active plans (1 year in either direction)
    private static final long MAX_ACTIVE_PLAN_RANGE_MILLIS = 365L * 24 * 60 * 60 * 1000;
    
    /**
     * Calculate the current day of a workout plan based on start date
     * @param startDateStr Start date of the workout plan assignment
     * @return Current day number (1-based), or -1 if start date is in the future or invalid
     */
    public static int getCurrentWorkoutDay(String startDateStr) {
        try {
            if (startDateStr == null || startDateStr.isEmpty()) {
                android.util.Log.d("DateUtils", "getCurrentWorkoutDay - startDate is null or empty");
                return -1;
            }
            
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
                android.util.Log.d("DateUtils", "getCurrentWorkoutDay - start date is in future");
                return -1;
            }
            
            // Calculate days difference and add 1 for 1-based indexing
            long daysDiff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
            int currentDay = (int) daysDiff + 1;
            
            // Validate that the calculated current day is reasonable
            // A workout plan shouldn't realistically be longer than MAX_WORKOUT_PLAN_DAYS
            if (currentDay > MAX_WORKOUT_PLAN_DAYS) {
                android.util.Log.d("DateUtils", "getCurrentWorkoutDay - current day is too far: " + currentDay + ", returning -1");
                return -1;
            }
            
            android.util.Log.d("DateUtils", "getCurrentWorkoutDay - calculated day: " + currentDay);
            return currentDay;
            
        } catch (Exception e) {
            android.util.Log.e("DateUtils", "getCurrentWorkoutDay - error occurred", e);
            return -1;
        }
    }
    
    /**
     * Check if a specific workout day is available for recording results
     * @param startDateStr Start date of the workout plan assignment
     * @param dayNumber Day number to check (1-based)
     * @return true if day is current or past, false if future or invalid
     */
    public static boolean isDayAvailableForRecording(String startDateStr, int dayNumber) {
        try {
            if (startDateStr == null || startDateStr.isEmpty()) {
                android.util.Log.d("DateUtils", "isDayAvailableForRecording - startDate is null or empty");
                return false;
            }
            
            // Validate dayNumber
            if (dayNumber <= 0 || dayNumber > MAX_WORKOUT_PLAN_DAYS) {
                android.util.Log.d("DateUtils", "isDayAvailableForRecording - invalid dayNumber: " + dayNumber);
                return false;
            }
            
            int currentDay = getCurrentWorkoutDay(startDateStr);
            
            // If current day is -1 (start date is in future or invalid), no day is available
            if (currentDay == -1) {
                android.util.Log.d("DateUtils", "isDayAvailableForRecording - current day is -1, returning false");
                return false;
            }
            
            boolean isAvailable = currentDay >= dayNumber;
            android.util.Log.d("DateUtils", "isDayAvailableForRecording - dayNumber: " + dayNumber + ", currentDay: " + currentDay + ", result: " + isAvailable);
            
            return isAvailable;
            
        } catch (Exception e) {
            android.util.Log.e("DateUtils", "isDayAvailableForRecording - error occurred", e);
            return false;
        }
    }
    
    /**
     * Check if a specific workout day is the current day
     * @param startDateStr Start date of the workout plan assignment
     * @param dayNumber Day number to check (1-based)
     * @return true if this is the current day
     */
    public static boolean isCurrentDay(String startDateStr, int dayNumber) {
        try {
            if (startDateStr == null || startDateStr.isEmpty()) {
                android.util.Log.d("DateUtils", "isCurrentDay - startDate is null or empty");
                return false;
            }
            
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
            
            // If start date is in the future, no day can be current
            if (diffInMillis < 0) {
                android.util.Log.d("DateUtils", "isCurrentDay - start date is in future, returning false");
                return false;
            }
            
            // Calculate days difference and add 1 for 1-based indexing
            long daysDiff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
            int currentDay = (int) daysDiff + 1;
            
            // Validate that the calculated current day is reasonable (not too far in the past)
            // A workout plan shouldn't realistically be longer than MAX_WORKOUT_PLAN_DAYS
            if (currentDay > MAX_WORKOUT_PLAN_DAYS) {
                android.util.Log.d("DateUtils", "isCurrentDay - current day is too far in the future: " + currentDay + ", returning false");
                return false;
            }
            
            // Validate that the requested dayNumber is reasonable
            if (dayNumber <= 0 || dayNumber > MAX_WORKOUT_PLAN_DAYS) {
                android.util.Log.d("DateUtils", "isCurrentDay - invalid dayNumber: " + dayNumber + ", returning false");
                return false;
            }
            
            boolean isCurrent = currentDay == dayNumber;
            
            android.util.Log.d("DateUtils", "isCurrentDay - dayNumber: " + dayNumber + ", currentDay: " + currentDay + ", result: " + isCurrent);
            
            return isCurrent;
            
        } catch (Exception e) {
            android.util.Log.e("DateUtils", "isCurrentDay - error occurred", e);
            return false;
        }
    }
    
    /**
     * Check if a workout plan is still active (not too old)
     * @param startDateStr Start date of the workout plan assignment
     * @return true if the plan is still within a reasonable timeframe
     */
    public static boolean isWorkoutPlanActive(String startDateStr) {
        try {
            if (startDateStr == null || startDateStr.isEmpty()) {
                return false;
            }
            
            Date startDate = parseDate(startDateStr);
            Date today = getCurrentDate();
            
            // Normalize start date to midnight
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            Date normalizedStartDate = startCal.getTime();
            
            long diffInMillis = today.getTime() - normalizedStartDate.getTime();
            
            // If start date is more than 1 year in the future, consider inactive
            if (diffInMillis < -MAX_ACTIVE_PLAN_RANGE_MILLIS) {
                return false;
            }
            
            // If start date is more than 1 year in the past, consider inactive
            if (diffInMillis > MAX_ACTIVE_PLAN_RANGE_MILLIS) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            android.util.Log.e("DateUtils", "isWorkoutPlanActive - error occurred", e);
            return false;
        }
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
