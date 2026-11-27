package com.example.myapplication;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String STATE_SELECTED_TAB = "selected_tab";
    private static final String[] TAB_TAGS = {"today", "calendar", "add", "insights"};
    private static final int[] TAB_IDS = {
            R.id.nav_today, R.id.nav_calendar, R.id.nav_add, R.id.nav_insights
    };

    private int selectedTab;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        TextView avatar = findViewById(R.id.avatar);
        String name = WelcomeActivity.getSavedName(this).trim();
        avatar.setText(name.isEmpty() ? "" : name.substring(0, 1).toUpperCase(java.util.Locale.ENGLISH));

        findViewById(R.id.nav_today).setOnClickListener(view -> showTab(0));
        findViewById(R.id.nav_calendar).setOnClickListener(view -> showTab(1));
        findViewById(R.id.nav_add).setOnClickListener(view -> showTab(2));
        findViewById(R.id.nav_insights).setOnClickListener(view -> showTab(3));

        if (savedInstanceState == null) {
            selectedTab = 0;
            showTab(0);
        } else {
            selectedTab = savedInstanceState.getInt(STATE_SELECTED_TAB, 0);
            activeFragment = getFragmentManager().findFragmentByTag(TAB_TAGS[selectedTab]);
            updateNavigation();
            if (activeFragment == null) {
                showTab(0);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_TAB, selectedTab);
        super.onSaveInstanceState(outState);
    }

    void showTodayTab() {
        showTab(0);
    }

    void showCalendarTab(String dateKey) {
        showTab(1);
        getFragmentManager().executePendingTransactions();
        Fragment calendar = getFragmentManager().findFragmentByTag(TAB_TAGS[1]);
        if (calendar instanceof Calendar) {
            ((Calendar) calendar).showDate(dateKey);
        }
    }

    void showCalendarTab() {
        showTab(1);
    }

    private void showTab(int tab) {
        if (tab == selectedTab && activeFragment != null) {
            updateNavigation();
            return;
        }

        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentByTag(TAB_TAGS[tab]);
        if (fragment == null) {
            fragment = createFragment(tab);
        }

        FragmentTransaction transaction = manager.beginTransaction();
        if (activeFragment != null && activeFragment.isAdded()) {
            transaction.hide(activeFragment);
        }
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.fragment_container, fragment, TAB_TAGS[tab]);
        }
        transaction.commit();

        activeFragment = fragment;
        selectedTab = tab;
        updateNavigation();
    }

    private Fragment createFragment(int tab) {
        switch (tab) {
            case 1:
                return new Calendar();
            case 2:
                return new AddTask();
            case 3:
                return new Insights();
            default:
                return new TodayFragment();
        }
    }

    private void updateNavigation() {
        for (int i = 0; i < TAB_IDS.length; i++) {
            LinearLayout item = findViewById(TAB_IDS[i]);
            boolean selected = i == selectedTab;
            item.setBackground(selected ? getDrawable(R.drawable.bg_nav_active) : null);
            for (int child = 0; child < item.getChildCount(); child++) {
                View childView = item.getChildAt(child);
                if (childView instanceof TextView) {
                    ((TextView) childView).setTextColor(selected
                            ? getColor(R.color.brand_blue)
                            : getColor(R.color.text_secondary));
                }
            }
        }
    }
}
