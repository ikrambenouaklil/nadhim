package com.example.myapplication.ui;

import android.app.Activity;
import android.content.Context;

import com.example.myapplication.ui.theme.ThemeManager;

/** Base Activity that loads Nadhim's saved appearance before inflating its views. */
public abstract class NadhimActivity extends Activity {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ThemeManager.wrap(base));
    }
}
