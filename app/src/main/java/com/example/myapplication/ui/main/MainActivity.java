package com.example.myapplication.ui.main;
import com.example.myapplication.R;
import com.example.myapplication.data.SessionManager;
import com.example.myapplication.ui.today.TodayFragment;
import com.example.myapplication.ui.calendar.CalendarFragment;
import com.example.myapplication.ui.addtask.AddTaskFragment;
import com.example.myapplication.ui.insights.InsightsFragment;
import com.example.myapplication.util.UiUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final String STATE_SELECTED_TAB = "selected_tab";
    private static final String[] TAB_TAGS = {"today", "calendar", "add", "insights"};
    private static final int[] TAB_IDS = {
            R.id.nav_today, R.id.nav_calendar, R.id.nav_add, R.id.nav_insights
    };

    private int selectedTab;
    private Fragment activeFragment;
    private FrameLayout hostRoot;
    private AssistantChat assistantChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hostRoot = findViewById(R.id.hostRoot);

        TextView avatar = findViewById(R.id.avatar);
        String name = SessionManager.getName(this).trim();
        avatar.setText(name.isEmpty() ? "" : name.substring(0, 1).toUpperCase(java.util.Locale.ENGLISH));

        findViewById(R.id.nav_today).setOnClickListener(view -> showTab(0));
        findViewById(R.id.nav_calendar).setOnClickListener(view -> showTab(1));
        findViewById(R.id.nav_add).setOnClickListener(view -> showTab(2));
        findViewById(R.id.nav_insights).setOnClickListener(view -> showTab(3));

        assistantChat = new AssistantChat(this, hostRoot);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (assistantChat != null) assistantChat.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_TAB, selectedTab);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (assistantChat != null && assistantChat.closeIfOpen()) return;
        super.onBackPressed();
    }

    public void showTodayTab() {
        showTab(0);
    }

    public void showCalendarTab(String dateKey) {
        showTab(1);
        getFragmentManager().executePendingTransactions();
        Fragment calendar = getFragmentManager().findFragmentByTag(TAB_TAGS[1]);
        if (calendar instanceof CalendarFragment) {
            ((CalendarFragment) calendar).showDate(dateKey);
        }
    }

    public void showCalendarTab() {
        showTab(1);
    }

    public void showAddTaskForDate(String dateKey) {
        showTab(2);
        getFragmentManager().executePendingTransactions();
        Fragment addTask = getFragmentManager().findFragmentByTag(TAB_TAGS[2]);
        if (addTask instanceof AddTaskFragment) {
            ((AddTaskFragment) addTask).setInitialDate(dateKey);
        }
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
                return new CalendarFragment();
            case 2:
                return new AddTaskFragment();
            case 3:
                return new InsightsFragment();
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
