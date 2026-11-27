package com.example.myapplication.ui.theme;

import android.content.Context;
import android.content.res.Configuration;

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
}
