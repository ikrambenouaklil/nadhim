package com.example.myapplication.ui.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.util.UiUtils;

import java.util.ArrayList;

/** Owns the floating assistant button and its chat panel. */
final class AssistantChat {
    private static final int REQUEST_ATTACHMENT = 72;

    private final Activity activity;
    private final FrameLayout hostRoot;
    private final ImageButton assistantButton;
    private final View chatOverlay;
    private final LinearLayout chatMessages;
    private final ScrollView chatScroll;
    private final LinearLayout pendingAttachmentsContainer;
    private final HorizontalScrollView pendingAttachmentsScroll;
    private final ArrayList<Uri> pendingAttachments = new ArrayList<>();

    AssistantChat(Activity activity, FrameLayout hostRoot) {
        this.activity = activity;
        this.hostRoot = hostRoot;
        chatOverlay = activity.getLayoutInflater().inflate(R.layout.activity_assistant_chat, hostRoot, false);
        chatOverlay.setVisibility(View.GONE);
        hostRoot.addView(chatOverlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        assistantButton = activity.findViewById(R.id.assistantButton);
        chatScroll = chatOverlay.findViewById(R.id.chatScroll);
        chatMessages = chatOverlay.findViewById(R.id.chatMessages);
        pendingAttachmentsContainer = chatOverlay.findViewById(R.id.pendingAttachmentsContainer);
        pendingAttachmentsScroll = chatOverlay.findViewById(R.id.pendingAttachmentsScroll);

        assistantButton.setOnClickListener(v -> openChat());
        assistantButton.setOnTouchListener(this::handleButtonTouch);
        chatOverlay.setOnClickListener(v -> closeChat());
        View panel = chatOverlay.findViewById(R.id.chatPanel);
        panel.setOnClickListener(v -> { });
        chatOverlay.findViewById(R.id.closeChatButton).setOnClickListener(v -> closeChat());
        chatOverlay.findViewById(R.id.attachButton).setOnClickListener(v -> showAttachmentOptions());
        TextView send = chatOverlay.findViewById(R.id.sendChatButton);
        android.widget.EditText input = chatOverlay.findViewById(R.id.chatInput);
        send.setOnClickListener(v -> sendMessage(input));
    }

    private boolean handleButtonTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.setTag(R.id.assistantButton, new float[]{event.getRawX(), event.getRawY(), view.getX(), view.getY(), 0});
                return true;
            case MotionEvent.ACTION_MOVE:
                float[] start = (float[]) view.getTag(R.id.assistantButton);
                float dx = event.getRawX() - start[0];
                float dy = event.getRawY() - start[1];
                if (Math.abs(dx) + Math.abs(dy) > UiUtils.dp(activity, 6)) start[4] = 1;
                float maxX = hostRoot.getWidth() - view.getWidth();
                float maxY = hostRoot.getHeight() - view.getHeight() - UiUtils.dp(activity, 64);
                view.setX(Math.max(0, Math.min(maxX, start[2] + dx)));
                view.setY(Math.max(UiUtils.dp(activity, 52), Math.min(maxY, start[3] + dy)));
                return true;
            case MotionEvent.ACTION_UP:
                float[] down = (float[]) view.getTag(R.id.assistantButton);
                if (down == null || down[4] == 0) view.performClick();
                return true;
            default:
                return false;
        }
    }

    private void sendMessage(android.widget.EditText input) {
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
    }

    private void showAttachmentOptions() {
        new AlertDialog.Builder(activity)
                .setTitle("Add an attachment")
                .setItems(new String[]{"Choose a photo", "Choose a file"}, (dialog, which) -> {
                    Intent picker = new Intent(Intent.ACTION_GET_CONTENT);
                    picker.setType(which == 0 ? "image/*" : "*/*");
                    picker.addCategory(Intent.CATEGORY_OPENABLE);
                    picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    try {
                        activity.startActivityForResult(Intent.createChooser(picker,
                                which == 0 ? "Choose a photo" : "Choose a file"), REQUEST_ATTACHMENT);
                    } catch (android.content.ActivityNotFoundException ignored) {
                        new AlertDialog.Builder(activity).setMessage("No file picker is available on this device.")
                                .setPositiveButton("OK", null).show();
                    }
                }).show();
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_ATTACHMENT || resultCode != Activity.RESULT_OK || data == null) return;
        if (data.getClipData() != null) {
            ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount(); i++) addPendingAttachment(clipData.getItemAt(i).getUri());
        } else if (data.getData() != null) {
            addPendingAttachment(data.getData());
        }
        renderPendingAttachments();
    }

    private void addPendingAttachment(Uri uri) {
        if (uri != null && !pendingAttachments.contains(uri)) pendingAttachments.add(uri);
    }

    private void renderPendingAttachments() {
        pendingAttachmentsContainer.removeAllViews();
        for (Uri uri : pendingAttachments) pendingAttachmentsContainer.addView(createAttachmentCard(uri, true));
        pendingAttachmentsScroll.setVisibility(pendingAttachments.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private View createAttachmentCard(Uri uri, boolean removable) {
        String name = attachmentName(uri);
        String mimeType = activity.getContentResolver().getType(uri);
        LinearLayout card = (LinearLayout) activity.getLayoutInflater()
                .inflate(R.layout.item_chat_attachment, pendingAttachmentsContainer, false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = removable ? Gravity.CENTER_VERTICAL : Gravity.END;
        params.bottomMargin = removable ? 0 : UiUtils.dp(activity, 10);
        params.rightMargin = removable ? UiUtils.dp(activity, 8) : 0;
        card.setLayoutParams(params);
        ImageView preview = card.findViewById(R.id.chatAttachmentImage);
        TextView icon = card.findViewById(R.id.chatAttachmentIcon);
        TextView fileName = card.findViewById(R.id.chatAttachmentName);
        TextView remove = card.findViewById(R.id.chatAttachmentRemove);
        fileName.setText(name);
        if (mimeType != null && mimeType.startsWith("image/")) {
            preview.setVisibility(View.VISIBLE);
            icon.setVisibility(View.GONE);
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
        try (Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) name = cursor.getString(index);
            }
        } catch (Exception ignored) { }
        if (name == null || name.trim().isEmpty()) name = uri.getLastPathSegment();
        return name == null || name.trim().isEmpty() ? "Selected attachment" : name;
    }

    private View chatBubble(String text, boolean fromUser) {
        TextView bubble = (TextView) activity.getLayoutInflater()
                .inflate(R.layout.item_chat_bubble, chatMessages, false);
        bubble.setText(text);
        bubble.setBackgroundResource(fromUser ? R.drawable.bg_primary_button : R.drawable.bg_chat_received);
        if (fromUser) bubble.setTextColor(android.graphics.Color.WHITE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = fromUser ? Gravity.END : Gravity.START;
        params.bottomMargin = UiUtils.dp(activity, 9);
        bubble.setLayoutParams(params);
        return bubble;
    }

    void openChat() {
        chatOverlay.setVisibility(View.VISIBLE);
        assistantButton.setVisibility(View.GONE);
    }

    void closeChat() {
        chatOverlay.setVisibility(View.GONE);
        assistantButton.setVisibility(View.VISIBLE);
    }

    boolean closeIfOpen() {
        if (chatOverlay.getVisibility() != View.VISIBLE) return false;
        closeChat();
        return true;
    }
}
