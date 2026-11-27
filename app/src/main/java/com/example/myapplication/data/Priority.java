package com.example.myapplication.data;

import java.util.Locale;

/** The supported task priority values. */
public enum Priority {
    NONE, LOW, MEDIUM, HIGH;

    public static Priority fromString(String value) {
        if (value == null || value.trim().isEmpty()) return NONE;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }

    public String storageValue() {
        return this == NONE ? "" : name();
    }

    public int insightsIndex() {
        switch (this) {
            case HIGH: return 0;
            case MEDIUM: return 1;
            case LOW: return 2;
            default: return 3;
        }
    }
}
