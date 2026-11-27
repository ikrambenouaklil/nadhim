package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
    private ImageButton assistantButton;
    private View chatOverlay;
    private LinearLayout chatMessages;
    private ScrollView chatScroll;
    private LinearLayout pendingAttachmentsContainer;
    private HorizontalScrollView pendingAttachmentsScroll;
    private final ArrayList<Uri> pendingAttachments = new ArrayList<>();
    private static final int REQUEST_CHAT_ATTACHMENT = 72;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        hostRoot = findViewById(R.id.hostRoot);

        TextView avatar = findViewById(R.id.avatar);
        String name = WelcomeActivity.getSavedName(this).trim();
        avatar.setText(name.isEmpty() ? "" : name.substring(0, 1).toUpperCase(java.util.Locale.ENGLISH));

        findViewById(R.id.nav_today).setOnClickListener(view -> showTab(0));
        findViewById(R.id.nav_calendar).setOnClickListener(view -> showTab(1));
        findViewById(R.id.nav_add).setOnClickListener(view -> showTab(2));
        findViewById(R.id.nav_insights).setOnClickListener(view -> showTab(3));

        createAssistantButton();

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

    private void createAssistantButton() {
        chatOverlay = createChatOverlay();
        chatOverlay.setVisibility(View.GONE);
        hostRoot.addView(chatOverlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        assistantButton = findViewById(R.id.assistantButton);
        assistantButton.setOnClickListener(v -> openChat());
        assistantButton.setOnTouchListener(new View.OnTouchListener() {
            float downRawX, downRawY, downX, downY;
            boolean moved;
            @Override public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downRawX = event.getRawX(); downRawY = event.getRawY();
                        downX = view.getX(); downY = view.getY(); moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - downRawX;
                        float dy = event.getRawY() - downRawY;
                        if (Math.abs(dx) + Math.abs(dy) > dp(6)) moved = true;
                        float maxX = hostRoot.getWidth() - view.getWidth();
                        float maxY = hostRoot.getHeight() - view.getHeight() - dp(64);
                        view.setX(Math.max(0, Math.min(maxX, downX + dx)));
                        view.setY(Math.max(dp(52), Math.min(maxY, downY + dy)));
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved) view.performClick();
                        return true;
                }
                return false;
            }
        });
    }

    private View createChatOverlay() {
        FrameLayout scrim = (FrameLayout) getLayoutInflater().inflate(R.layout.activity_assistant_chat, hostRoot, false);
        scrim.setOnClickListener(v -> closeChat());
        View panel = scrim.findViewById(R.id.chatPanel);
        panel.setOnClickListener(v -> { });
        chatScroll = scrim.findViewById(R.id.chatScroll);
        chatMessages = scrim.findViewById(R.id.chatMessages);
        pendingAttachmentsContainer = scrim.findViewById(R.id.pendingAttachmentsContainer);
        pendingAttachmentsScroll = scrim.findViewById(R.id.pendingAttachmentsScroll);
        scrim.findViewById(R.id.closeChatButton).setOnClickListener(v -> closeChat());
        scrim.findViewById(R.id.attachButton).setOnClickListener(v -> showAttachmentOptions());
        TextView send = scrim.findViewById(R.id.sendChatButton);
        android.widget.EditText input = scrim.findViewById(R.id.chatInput);
        send.setOnClickListener(v -> {
            String message = input.getText().toString().trim();
            if (message.isEmpty() && pendingAttachments.isEmpty()) return;
            for (Uri attachment : new ArrayList<>(pendingAttachments)) {
                chatMessages.addView(createAttachmentCard(attachment, false));
            }
            if (!message.isEmpty()) chatMessages.addView(chatBubble(message, true));
            chatMessages.addView(chatBubble("Thanks! AI chat is not connected yet, so I can’t organize this message automatically for now.", false));
            input.setText("");
            pendingAttachments.clear();
            renderPendingAttachments();
            chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
        });
        return scrim;
    }

    private void showAttachmentOptions() {
        new AlertDialog.Builder(this)
                .setTitle("Add an attachment")
                .setItems(new String[]{"Choose a photo", "Choose a file"}, (dialog, which) -> {
                    Intent picker = new Intent(Intent.ACTION_GET_CONTENT);
                    picker.setType(which == 0 ? "image/*" : "*/*");
                    picker.addCategory(Intent.CATEGORY_OPENABLE);
                    picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    try {
                        startActivityForResult(Intent.createChooser(picker,
                                which == 0 ? "Choose a photo" : "Choose a file"), REQUEST_CHAT_ATTACHMENT);
                    } catch (android.content.ActivityNotFoundException ignored) {
                        new AlertDialog.Builder(this).setMessage("No file picker is available on this device.")
                                .setPositiveButton("OK", null).show();
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CHAT_ATTACHMENT || resultCode != RESULT_OK || data == null) return;
        if (data.getClipData() != null) {
            android.content.ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                addPendingAttachment(clipData.getItemAt(i).getUri());
            }
        } else if (data.getData() != null) {
            addPendingAttachment(data.getData());
        }
        renderPendingAttachments();
    }

    private void addPendingAttachment(Uri uri) {
        if (uri != null && !pendingAttachments.contains(uri)) pendingAttachments.add(uri);
    }

    private void renderPendingAttachments() {
        if (pendingAttachmentsContainer == null || pendingAttachmentsScroll == null) return;
        pendingAttachmentsContainer.removeAllViews();
        for (Uri uri : pendingAttachments) {
            pendingAttachmentsContainer.addView(createAttachmentCard(uri, true));
        }
        pendingAttachmentsScroll.setVisibility(pendingAttachments.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private LinearLayout createAttachmentCard(Uri uri, boolean removable) {
        String name = attachmentName(uri);
        String mimeType = getContentResolver().getType(uri);
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(dp(10), dp(8), dp(12), dp(8));
        card.setBackground(roundBackground(Color.rgb(239, 246, 255), Color.rgb(191, 219, 254), 14));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.gravity = removable ? Gravity.CENTER_VERTICAL : Gravity.END;
        cardParams.bottomMargin = removable ? 0 : dp(10);
        cardParams.rightMargin = removable ? dp(8) : 0;
        card.setLayoutParams(cardParams);

        if (mimeType != null && mimeType.startsWith("image/")) {
            ImageView preview = new ImageView(this);
            preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            try { preview.setImageURI(uri); } catch (RuntimeException ignored) { }
            card.addView(preview, new LinearLayout.LayoutParams(dp(54), dp(54)));
        } else {
            TextView fileIcon = label("▤", 22, Color.rgb(37, 99, 235), true);
            fileIcon.setGravity(Gravity.CENTER);
            card.addView(fileIcon, new LinearLayout.LayoutParams(dp(42), dp(42)));
        }
        TextView fileName = label(name, 13, Color.rgb(30, 64, 110), true);
        fileName.setMaxWidth(dp(190));
        fileName.setMaxLines(2);
        fileName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(-2, -2);
        nameParams.leftMargin = dp(9);
        card.addView(fileName, nameParams);
        if (removable) {
            TextView remove = label("×", 22, Color.rgb(100, 116, 139), false);
            remove.setGravity(Gravity.CENTER);
            remove.setContentDescription("Remove " + name);
            LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(dp(28), dp(42));
            removeParams.leftMargin = dp(6);
            card.addView(remove, removeParams);
            remove.setOnClickListener(v -> {
                pendingAttachments.remove(uri);
                renderPendingAttachments();
            });
        }
        return card;
    }

    private String attachmentName(Uri uri) {
        String name = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) name = cursor.getString(nameIndex);
            }
        } catch (Exception ignored) { }
        if (name == null || name.trim().isEmpty()) name = uri.getLastPathSegment();
        return name == null || name.trim().isEmpty() ? "Selected attachment" : name;
    }

    private TextView chatBubble(String text, boolean fromUser) {
        TextView bubble = label(text, 14, fromUser ? Color.WHITE : Color.rgb(51, 65, 85), false);
        bubble.setPadding(dp(13), dp(10), dp(13), dp(10));
        bubble.setBackground(roundBackground(fromUser ? Color.rgb(37, 99, 235) : Color.rgb(241, 245, 249), Color.TRANSPARENT, 16));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = fromUser ? Gravity.END : Gravity.START;
        params.bottomMargin = dp(9);
        bubble.setLayoutParams(params);
        return bubble;
    }

    private TextView label(String text, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text); view.setTextSize(size); view.setTextColor(color);
        if (bold) view.setTypeface(null, Typeface.BOLD);
        return view;
    }

    private GradientDrawable roundBackground(int color, int stroke, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color); drawable.setCornerRadius(dp(radiusDp));
        if (stroke != Color.TRANSPARENT) drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private void openChat() {
        chatOverlay.setVisibility(View.VISIBLE);
        assistantButton.setVisibility(View.GONE);
    }

    private void closeChat() {
        chatOverlay.setVisibility(View.GONE);
        assistantButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_TAB, selectedTab);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (chatOverlay != null && chatOverlay.getVisibility() == View.VISIBLE) {
            closeChat();
            return;
        }
        super.onBackPressed();
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
