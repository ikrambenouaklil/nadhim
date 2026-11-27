package com.example.myapplication;

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

    private View createAttachmentCard(Uri uri, boolean removable) {
        String name = attachmentName(uri);
        String mimeType = getContentResolver().getType(uri);
        LinearLayout card = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.item_chat_attachment, pendingAttachmentsContainer, false);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.gravity = removable ? Gravity.CENTER_VERTICAL : Gravity.END;
        cardParams.bottomMargin = removable ? 0 : dp(10);
        cardParams.rightMargin = removable ? dp(8) : 0;
        card.setLayoutParams(cardParams);
        ImageView preview = card.findViewById(R.id.chatAttachmentImage);
        TextView fileIcon = card.findViewById(R.id.chatAttachmentIcon);
        TextView fileName = card.findViewById(R.id.chatAttachmentName);
        TextView remove = card.findViewById(R.id.chatAttachmentRemove);
        fileName.setText(name);
        if (mimeType != null && mimeType.startsWith("image/")) {
            preview.setVisibility(View.VISIBLE);
            fileIcon.setVisibility(View.GONE);
            try { preview.setImageURI(uri); } catch (RuntimeException ignored) { }
        }
        if (removable) {
            remove.setVisibility(View.VISIBLE);
            remove.setContentDescription("Remove " + name);
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

    private View chatBubble(String text, boolean fromUser) {
        TextView bubble = (TextView) getLayoutInflater()
                .inflate(R.layout.item_chat_bubble, chatMessages, false);
        bubble.setText(text);
        if (fromUser) bubble.setBackgroundResource(R.drawable.bg_chat_user_bubble);
        else bubble.setBackgroundResource(R.drawable.bg_chat_received);
        if (fromUser) bubble.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = fromUser ? Gravity.END : Gravity.START;
        params.bottomMargin = dp(9);
        bubble.setLayoutParams(params);
        return bubble;
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

    void showAddTaskForDate(String dateKey) {
        showTab(2);
        getFragmentManager().executePendingTransactions();
        Fragment addTask = getFragmentManager().findFragmentByTag(TAB_TAGS[2]);
        if (addTask instanceof AddTask) {
            ((AddTask) addTask).setInitialDate(dateKey);
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
