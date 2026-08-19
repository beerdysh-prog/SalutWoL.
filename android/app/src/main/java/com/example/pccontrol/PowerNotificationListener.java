package com.example.pccontrol;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PowerNotificationListener extends NotificationListenerService {
    private static final String TITLE = "История сценариев";
    private static final String ON = "Включи компьютер";
    private static final String OFF = "Выключи компьютер";
    private static final long COOLDOWN_MS = 10_000L;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile long lastActionAt = 0;
    private volatile String lastNotificationKey = "";
    private volatile String lastCommand = "";

    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        Notification n = sbn.getNotification();
        if (n == null || n.extras == null) return;

        CharSequence titleCs = n.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence textCs = n.extras.getCharSequence(Notification.EXTRA_TEXT);
        String title = titleCs == null ? "" : titleCs.toString().trim();
        String text = textCs == null ? "" : textCs.toString().trim();

        if (!TITLE.equals(title)) return;
        String command = null;
        if (ON.equals(text)) command = "ON";
        else if (OFF.equals(text)) command = "OFF";
        if (command == null) return;

        long now = System.currentTimeMillis();
        String key = sbn.getKey();
        if (command.equals(lastCommand) && key.equals(lastNotificationKey) && now - lastActionAt < COOLDOWN_MS) return;
        if (now - lastActionAt < COOLDOWN_MS) return;

        lastActionAt = now;
        lastNotificationKey = key;
        lastCommand = command;
        final String action = command;
        executor.execute(() -> executeAction(action));
    }

    private void executeAction(String action) {
        Settings s = new Settings(this);
        try {
            if ("ON".equals(action)) {
                if (!s.onEnabled()) return;
                if (TextUtils.isEmpty(s.mac())) throw new IllegalArgumentException("MAC не задан");
                WakeOnLan.send(s.mac(), s.broadcast());
            } else {
                if (!s.offEnabled()) return;
                PowerClient.shutdown(s.pcIp(), s.secret());
            }
        } catch (Exception ignored) {
        }
    }

    @Override public void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
