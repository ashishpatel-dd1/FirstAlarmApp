package com.example.simplealarmapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TimePicker;
import android.widget.DatePicker;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.slider.Slider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.slider.Slider;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SwitchMaterial alarmSwitch;
    private TextView timePicker;
    private TextView datePicker;
    private MaterialCheckBox recurringCheckBox;
    private Slider snoozeSlider;
    //private SeekBar snoozeSeekBar;
    private SwitchMaterial snoozeSwitch;
    private TextView feedbackText;
    private MaterialButton setAlarmButton;
    private MaterialButton cancelAlarmButton;
    private MaterialButton stopAlarmButton;

    private boolean isSnoozeEnabled = true;
    private int snoozeDuration = 5; // Default snooze duration (in minutes)
    private boolean isAlarmEnabled = false;
    private final Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        alarmSwitch = findViewById(R.id.alarmSwitch);
        timePicker = findViewById(R.id.timePicker);
        datePicker = findViewById(R.id.datePicker); // Added Date Picker TextView
        recurringCheckBox = findViewById(R.id.recurringCheckBox);
        snoozeSwitch = findViewById(R.id.snoozeSwitch);
        //snoozeSeekBar = findViewById(R.id.snoozeSeekBar);
        snoozeSlider = findViewById(R.id.snoozeSlider);
        feedbackText = findViewById(R.id.feedbackText);
        setAlarmButton = findViewById(R.id.setAlarmButton);
        cancelAlarmButton = findViewById(R.id.cancelAlarmButton);
        stopAlarmButton = findViewById(R.id.stopAlarmButton);

        // Set initial values
        snoozeSlider.setValue(snoozeDuration);
        feedbackText.setText(getString(R.string.no_alarms_set));
        alarmSwitch.setChecked(isAlarmEnabled);

        // Snooze SeekBar Listener
//        snoozeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                snoozeDuration = progress;
//                Toast.makeText(MainActivity.this, "Snooze Duration: " + snoozeDuration + " min", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//        });



        // Inside your MainActivity or relevant class
        snoozeSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                snoozeDuration = (int) value; // Get the current value as an integer
                Toast.makeText(MainActivity.this, "Snooze Duration: " + snoozeDuration + " min", Toast.LENGTH_SHORT).show();
            }
        });


        // Snooze Switch Listener
        snoozeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isSnoozeEnabled = isChecked;
            String status = isSnoozeEnabled ? "enabled" : "disabled";
            Toast.makeText(MainActivity.this, "Snooze is " + status, Toast.LENGTH_SHORT).show();
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

            boolean isRecurring = recurringCheckBox.isChecked();
            setAlarm(selectedDateTime, isRecurring);

            String message = "Alarm set for: " + selectedDateTime.getTime() +
                    (isRecurring ? " (Recurring Daily)" : "");
            feedbackText.setText(message);
        });

        // Cancel Alarm Button Listener
        cancelAlarmButton.setOnClickListener(v -> {
            cancelAlarm();
            feedbackText.setText(R.string.alarm_canceled);
        });

        // Stop Alarm Button Listener
        stopAlarmButton.setOnClickListener(v -> {
            stopAlarm();
            disableSnooze();
            feedbackText.setText(R.string.alarm_stopped);
        });

        // Time Picker Click Listener
        timePicker.setOnClickListener(v -> showTimePickerDialog());

        // Date Picker Click Listener
        datePicker.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showTimePickerDialog() {
        int hour = selectedDateTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDateTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute1);
            selectedDateTime.set(Calendar.SECOND, 0);
            timePicker.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1));
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void showDatePickerDialog() {
        int year = selectedDateTime.get(Calendar.YEAR);
        int month = selectedDateTime.get(Calendar.MONTH);
        int day = selectedDateTime.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            selectedDateTime.set(Calendar.YEAR, year1);
            selectedDateTime.set(Calendar.MONTH, month1);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            datePicker.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth));

        }, year, month, day);

        datePickerDialog.show();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(Calendar calendar, boolean isRecurring) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("snoozeEnabled", isSnoozeEnabled);
        intent.putExtra("snoozeDuration", snoozeDuration);
        intent.putExtra("alarmTimeMillis", calendar.getTimeInMillis());

        // Generate a unique request code
        int uniqueRequestCode = (int) System.currentTimeMillis(); // Unique code for each alarm

        // Store the unique request code
        SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("last_alarm_request_code", uniqueRequestCode);
        editor.apply();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, uniqueRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

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
        SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        int savedRequestCode = sharedPreferences.getInt("last_alarm_request_code", -1);

        if (savedRequestCode != -1) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, AlarmReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    savedRequestCode,  // Use the saved request code to cancel the correct alarm
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);  // Cancel the alarm
                Toast.makeText(this, "Alarm canceled.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No alarm to cancel.", Toast.LENGTH_SHORT).show();
        }
    }


    private void stopAlarm() {
        if (AlarmReceiver.mediaPlayer != null && AlarmReceiver.mediaPlayer.isPlaying()) {
            AlarmReceiver mediaPlayer = new AlarmReceiver();
            mediaPlayer.stopAlarm();
        }
    }

    private void disableSnooze() {
        snoozeSwitch.setChecked(false);
        isSnoozeEnabled = false;
        snoozeSlider.setValue(5);
        snoozeDuration = 5;
        Toast.makeText(MainActivity.this, "Snooze has been disabled.", Toast.LENGTH_SHORT).show();
    }
}
