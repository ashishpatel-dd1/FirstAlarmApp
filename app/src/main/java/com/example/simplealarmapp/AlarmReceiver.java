package com.example.simplealarmapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "ALARM_CHANNEL";
    private static final int NOTIFICATION_ID = 1;
    public static MediaPlayer mediaPlayer;
    private final Handler stopAlarmHandler = new Handler(Looper.getMainLooper());
    private final Runnable stopAlarmRunnable = this::stopAlarm;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isSnoozeEnabled = intent.getBooleanExtra("snoozeEnabled", true);
        int snoozeDuration = intent.getIntExtra("snoozeDuration", 5);
        //long alarmTimeMillis = intent.getLongExtra("alarmTimeMillis", 0);

        // Play custom alarm sound
        mediaPlayer = MediaPlayer.create(context, Settings.System.DEFAULT_ALARM_ALERT_URI);
        mediaPlayer.start();

        // Automatically stop alarm after 1 minute
        stopAlarmHandler.postDelayed(stopAlarmRunnable, 60 * 1000);

        // Create a notification
        createNotification(context, isSnoozeEnabled, snoozeDuration);
    }

    private void createNotification(Context context, boolean isSnoozeEnabled, int snoozeDuration) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel (for Android O and above)
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Channel for Alarm Notifications");
        notificationManager.createNotificationChannel(channel);

        // Build Snooze action for the notification
        PendingIntent snoozePendingIntent = null;
        if (isSnoozeEnabled) {
            Intent snoozeIntent = new Intent(context, SnoozeReceiver.class);
            snoozeIntent.putExtra("snoozeDuration", snoozeDuration);

            snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        }

        // Notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Alarm Triggered")
                .setContentText("Tap to snooze or dismiss the alarm.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (isSnoozeEnabled && snoozePendingIntent != null) {
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Snooze", snoozePendingIntent);
        }

        // Notify the user
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Stops the alarm sound when snooze is triggered or canceled
    public void stopAlarm() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;

            // Cancel the stop alarm task if called manually
            stopAlarmHandler.removeCallbacks(stopAlarmRunnable);
        }
    }
}
