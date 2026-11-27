package com.example.myapplication.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {
    private static final String KEY_PATTERN = "yyyy-MM-dd";

    private DateUtils() { }

    public static String dateKey(Calendar calendar) {
        return new SimpleDateFormat(KEY_PATTERN, Locale.US).format(calendar.getTime());
    }

    public static String todayKey() {
        return dateKey(Calendar.getInstance());
    }

    public static Date parseDateKey(String key) throws ParseException {
        return new SimpleDateFormat(KEY_PATTERN, Locale.US).parse(key);
    }

    public static String formatShort(String key) {
        return formatDate(key, "MMM d");
    }

    public static String formatLong(String key) {
        return formatDate(key, "EEE, MMM d, yyyy");
    }

    private static String formatDate(String key, String pattern) {
        if (key == null) return "";
        try {
            Date date = parseDateKey(key);
            return date == null ? key : new SimpleDateFormat(pattern, Locale.ENGLISH).format(date);
        } catch (ParseException ignored) {
            return key;
        }
    }

    public static String formatTime(int totalMinutes) {
        int safe = ((totalMinutes % (24 * 60)) + (24 * 60)) % (24 * 60);
        return String.format(Locale.US, "%02d:%02d", safe / 60, safe % 60);
    }

    public static String formatDuration(int minutes) {
        int hours = minutes / 60;
        int remaining = minutes % 60;
        if (hours == 0) return remaining + " min";
        if (remaining == 0) return hours + (hours == 1 ? " hour" : " hours");
        return hours + " h " + remaining + " min";
    }
}
