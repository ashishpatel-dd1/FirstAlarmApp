package com.example.simplealarmapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

        // Retrieve the saved unique request code for the alarm
        SharedPreferences sharedPreferences = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        int savedRequestCode = sharedPreferences.getInt("last_alarm_request_code", -1);

        if (savedRequestCode == -1) {
            Toast.makeText(context, "Unable to retrieve alarm request code.", Toast.LENGTH_SHORT).show();
            return;
        }

        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                context,
                savedRequestCode,  // Use the saved request code
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
                        Toast.makeText(context, "Alarm snoozed for " + snoozeDuration + " minutes", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Exact alarms permission not granted!", Toast.LENGTH_SHORT).show();
                        Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(settingsIntent);
                    }
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
                    Toast.makeText(context, "Alarm snoozed for " + snoozeDuration + " minutes", Toast.LENGTH_SHORT).show();
                }
            } catch (SecurityException e) {
                Toast.makeText(context, "Unable to schedule snooze: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "Unable to access AlarmManager service!", Toast.LENGTH_SHORT).show();
        }
    }
}
