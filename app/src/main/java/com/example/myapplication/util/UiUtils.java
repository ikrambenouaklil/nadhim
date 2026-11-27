package com.example.myapplication.util;

import android.content.Context;

public final class UiUtils {
    private UiUtils() { }

    public static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
