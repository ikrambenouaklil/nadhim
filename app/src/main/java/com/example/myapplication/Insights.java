package com.example.myapplication;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Insights extends Fragment {

    // ---- Thème Nadhim ----
    private static final int BLUE = Color.rgb(37, 99, 235);
    private static final int BLUE_DARK = Color.rgb(29, 78, 216);
    private static final int BLUE_LIGHT = Color.rgb(96, 165, 250);
    private static final int LIME = Color.rgb(163, 230, 0);
    private static final int PURPLE = Color.rgb(147, 51, 234);
    private static final int DARK = Color.rgb(51, 65, 85);
    private static final int MUTED = Color.rgb(100, 116, 139);

    // ---- Données (exemple : à remplacer par tes vraies données) ----
    private static final String TASKS_DONE = "12";
    private static final String ON_TIME = "92%";
    private static final String FOCUS_TIME = "4.5h";
    private static final String BEST_FOCUS_TIME = "9:00 AM – 11:00 AM";

    private static final String[] DAYS = {"M", "T", "W", "T", "F", "S", "S"};
    private static final int[] ACTIVITY = {5, 8, 6, 11, 9, 12, 7}; // tâches par jour

    private static final String[] CATEGORY_NAMES = {"Work", "Study", "Personal"};
    private static final int[] CATEGORY_PERCENT = {48, 32, 20};
    private static final int[] CATEGORY_COLORS = {BLUE, LIME, PURPLE};

    private static final int CHART_BAR_MAX_DP = 130;

    private LinearLayout weeklyBars;
    private LinearLayout categoriesContainer;
    private View contentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.activity_insights, container, false);
        bindViews();
        renderStats();
        renderWeeklyChart();
        renderCategories();
        renderFocusCard();
        return contentView;
    }

    private void bindViews() {
        weeklyBars = findViewById(R.id.weeklyBars);
        categoriesContainer = findViewById(R.id.categoriesContainer);
    }

    // =====================================================================
    // Affichage
    // =====================================================================

    private void renderStats() {
        ((TextView) findViewById(R.id.statTasksValue)).setText(TASKS_DONE);
        ((TextView) findViewById(R.id.statOnTimeValue)).setText(ON_TIME);
        ((TextView) findViewById(R.id.statFocusValue)).setText(FOCUS_TIME);
    }

    private void renderWeeklyChart() {
        weeklyBars.removeAllViews();

        int max = 1;
        int maxIndex = 0;
        for (int i = 0; i < ACTIVITY.length; i++) {
            if (ACTIVITY[i] > max) {
                max = ACTIVITY[i];
                maxIndex = i;
            }
        }

        for (int i = 0; i < DAYS.length; i++) {
            LinearLayout column = new LinearLayout(getActivity());
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            column.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1));

            // Barre
            int barHeight = Math.max(dp(14), dp(CHART_BAR_MAX_DP) * ACTIVITY[i] / max);
            View bar = new View(getActivity());
            bar.setBackground(i == maxIndex ? barDrawable(LIME, LIME) : barDrawable(BLUE_LIGHT, BLUE));
            bar.setLayoutParams(new LinearLayout.LayoutParams(dp(18), barHeight));

            // Libellé du jour
            TextView label = new TextView(getActivity());
            label.setText(DAYS[i]);
            label.setTextColor(MUTED);
            label.setTextSize(11);
            label.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = dp(10);
            label.setLayoutParams(labelParams);

            final int dayIndex = i;
            final int value = ACTIVITY[i];
            column.setOnClickListener(v -> Toast.makeText(getActivity(),
                    value + " tasks on day " + (dayIndex + 1), Toast.LENGTH_SHORT).show());

            column.addView(bar);
            column.addView(label);
            weeklyBars.addView(column);
        }
    }

    private void renderCategories() {
        categoriesContainer.removeAllViews();
        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            LinearLayout row = new LinearLayout(getActivity());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.topMargin = dp(i == 0 ? 0 : 14);
            row.setLayoutParams(rowParams);

            // Pastille de couleur
            View dot = new View(getActivity());
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(CATEGORY_COLORS[i]);
            dot.setBackground(shape);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(10), dp(10));
            dotParams.setMarginEnd(dp(12));
            dot.setLayoutParams(dotParams);

            TextView name = new TextView(getActivity());
            name.setText(CATEGORY_NAMES[i]);
            name.setTextColor(DARK);
            name.setTextSize(13);
            name.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView percent = new TextView(getActivity());
            percent.setText(CATEGORY_PERCENT[i] + "%");
            percent.setTextColor(MUTED);
            percent.setTextSize(12);

            row.addView(dot);
            row.addView(name);
            row.addView(percent);
            categoriesContainer.addView(row);
        }
    }

    private void renderFocusCard() {
        View card = findViewById(R.id.focusCard);
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{BLUE_DARK, BLUE});
        gradient.setCornerRadius(dp(16));
        card.setBackground(gradient);
        ((TextView) findViewById(R.id.focusTimeText)).setText(BEST_FOCUS_TIME);
    }

    // =====================================================================
    // Utilitaires
    // =====================================================================

    /** Barre arrondie avec dégradé vertical. */
    private GradientDrawable barDrawable(int topColor, int bottomColor) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, new int[]{topColor, bottomColor});
        d.setCornerRadius(dp(9));
        return d;
    }

    private int dp(int value) {
        return Math.round(value * getActivity().getResources().getDisplayMetrics().density);
    }

    private <T extends View> T findViewById(int id) {
        return contentView.findViewById(id);
    }
}