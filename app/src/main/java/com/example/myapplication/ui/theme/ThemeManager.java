package com.example.myapplication.ui.theme;

import android.content.Context;
import android.content.res.Configuration;
import android.app.Activity;
import android.widget.ImageButton;

import com.example.myapplication.R;
import com.example.myapplication.data.SessionManager;

/** Applies the user's saved light or dark appearance to framework Activities. */
public final class ThemeManager {
    private ThemeManager() { }

    public static Context wrap(Context base) {
        Configuration configuration = new Configuration(base.getResources().getConfiguration());
        int mask = configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK;
        configuration.uiMode = mask | (SessionManager.isDarkMode(base)
                ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO);
        return base.createConfigurationContext(configuration);
    }

    public static boolean needsRecreate(Context context) {
        int appliedMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        boolean showingDark = appliedMode == Configuration.UI_MODE_NIGHT_YES;
        return showingDark != SessionManager.isDarkMode(context);
    }

    /** Wires the XML theme button to the saved theme preference. */
    public static void bindToggle(Activity activity) {
        ImageButton toggle = activity.findViewById(R.id.themeToggle);
        if (toggle == null) return;
        updateToggleIcon(activity, toggle);
        toggle.setOnClickListener(view -> {
            SessionManager.setDarkMode(activity, !SessionManager.isDarkMode(activity));
            activity.recreate();
        });
    }

    private static void updateToggleIcon(Activity activity, ImageButton toggle) {
        boolean dark = SessionManager.isDarkMode(activity);
        toggle.setImageResource(dark ? R.drawable.ic_theme_sun : R.drawable.ic_theme_moon);
        toggle.setContentDescription(activity.getString(dark
                ? R.string.switch_to_light_mode : R.string.switch_to_dark_mode));
    }
}
