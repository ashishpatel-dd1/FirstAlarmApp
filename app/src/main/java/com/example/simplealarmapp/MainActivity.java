package com.example.simplealarmapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private CheckBox recurringCheckBox;
    private SeekBar snoozeSeekBar;
    private Switch alarmSwitch;
    private TextView feedbackText;
    private Switch snoozeSwitch;
    private boolean isSnoozeEnabled = true; // Snooze is enabled by default
    private int snoozeDuration = 5; // Default snooze duration (in minutes)
    private boolean isAlarmEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        timePicker = findViewById(R.id.timePicker);
        recurringCheckBox = findViewById(R.id.recurringCheckBox);
        snoozeSwitch = findViewById(R.id.snoozeSwitch);
        snoozeSeekBar = findViewById(R.id.snoozeSeekBar);
        alarmSwitch = findViewById(R.id.alarmSwitch);
        feedbackText = findViewById(R.id.feedbackText);

        Button setAlarmButton = findViewById(R.id.setAlarmButton);
        Button cancelAlarmButton = findViewById(R.id.cancelAlarmButton);
        Button stopAlarmButton = findViewById(R.id.stopAlarmButton);  // Added stop alarm button

        // Set initial values
        snoozeSeekBar.setProgress(snoozeDuration);
        feedbackText.setText(getString(R.string.no_alarms_set));
        alarmSwitch.setChecked(isAlarmEnabled);

        // Snooze SeekBar Listener
        snoozeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                snoozeDuration = progress;
                Toast.makeText(MainActivity.this, "Snooze Duration: " + snoozeDuration + " min", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Snooze Switch Listener
        snoozeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isSnoozeEnabled = isChecked;
            String status = isSnoozeEnabled ? "enabled" : "disabled";
            Toast.makeText(MainActivity.this, "Snooze is " + status, Toast.LENGTH_SHORT).show();
            if (isSnoozeEnabled) {
                stopAlarm();  // Stop the alarm sound when snooze is enabled
            }
        });

        // Alarm Switch Listener
        alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isAlarmEnabled = isChecked;
            String status = isAlarmEnabled ? "enabled" : "disabled";
            Toast.makeText(MainActivity.this, "Alarm is " + status, Toast.LENGTH_SHORT).show();
        });

        // Set Alarm Button Listener
        setAlarmButton.setOnClickListener(v -> {
            if (!isAlarmEnabled) {
                Toast.makeText(MainActivity.this, "Enable the alarm first.", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            calendar.set(Calendar.MINUTE, timePicker.getMinute());
            calendar.set(Calendar.SECOND, 0);

            boolean isRecurring = recurringCheckBox.isChecked();
            setAlarm(calendar, isRecurring);

            String message = "Alarm set for: " + calendar.getTime() +
                    (isRecurring ? " (Recurring Daily)" : "");
            feedbackText.setText(message);
        });

        // Cancel Alarm Button Listener
        cancelAlarmButton.setOnClickListener(v -> {
            cancelAlarm();
            feedbackText.setText(R.string.alarm_canceled);
        });

        // Stop Alarm Button Listener (Stops the ringing alarm and disables snooze)
        stopAlarmButton.setOnClickListener(v -> {
            stopAlarm();  // Stop the alarm sound
            disableSnooze();  // Disable snooze functionality
            feedbackText.setText(R.string.alarm_stopped);  // Update feedback text
        });
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(Calendar calendar, boolean isRecurring) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("snoozeEnabled", isSnoozeEnabled); // Pass snooze state
        intent.putExtra("snoozeDuration", snoozeDuration); // Pass snooze duration to the receiver
        intent.putExtra("alarmTimeMillis", calendar.getTimeInMillis()); // Pass alarm time

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PendingIntent.FLAG_IMMUTABLE
                : 0;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags);

        if (alarmManager != null) {
            if (isRecurring) {
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                );
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
            Toast.makeText(this, "Alarm set successfully.", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PendingIntent.FLAG_IMMUTABLE
                : 0;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(this, "Alarm canceled.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to stop the alarm sound
    private void stopAlarm() {
        if (AlarmReceiver.mediaPlayer != null && AlarmReceiver.mediaPlayer.isPlaying()) {
            AlarmReceiver mediaPlayer = new AlarmReceiver();
            mediaPlayer.stopAlarm();  // Stop the alarm sound
        }
    }

    // Method to disable snooze functionality and reset SeekBar
    private void disableSnooze() {
        snoozeSwitch.setChecked(false);  // Turn off the snooze switch
        isSnoozeEnabled = false;  // Update the snooze state

        // Optionally, reset the SeekBar to its default position
        snoozeSeekBar.setProgress(5);  // Reset to default (5 minutes)
        snoozeDuration = 5;  // Reset the snooze duration to 5 minutes

        // Notify the user that snooze has been disabled
        Toast.makeText(MainActivity.this, "Snooze has been disabled.", Toast.LENGTH_SHORT).show();
    }
}
