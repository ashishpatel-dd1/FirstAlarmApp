package com.example.simplealarmapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import java.util.Calendar;

public class SnoozeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int snoozeDuration = intent.getIntExtra("snoozeDuration", 5);

        // Set snooze time
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, snoozeDuration);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);

        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            try {
                // Check if exact alarms can be scheduled (for Android S and above)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        // Schedule exact snooze alarm
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
                        Toast.makeText(context, "Alarm snoozed for " + snoozeDuration + " minutes", Toast.LENGTH_SHORT).show();
                    } else {
                        // Notify the user about missing permission
                        Toast.makeText(context, "Exact alarms permission not granted!", Toast.LENGTH_SHORT).show();

                        // Open system settings to request the permission
                        Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(settingsIntent);
                    }
                } else {
                    // Schedule the alarm directly for pre-Android S devices
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
                    Toast.makeText(context, "Alarm snoozed for " + snoozeDuration + " minutes", Toast.LENGTH_SHORT).show();
                }
            } catch (SecurityException e) {
                // Handle unexpected security exceptions gracefully
                Toast.makeText(context, "Unable to schedule snooze: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            // Handle the case where the AlarmManager is null
            Toast.makeText(context, "Unable to access AlarmManager service!", Toast.LENGTH_SHORT).show();
        }
    }
}
