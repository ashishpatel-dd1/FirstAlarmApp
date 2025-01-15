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
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import com.example.simplealarmapp.AlarmReceiver;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TimePicker timePicker = findViewById(R.id.timePicker);
        CheckBox recurringCheckBox = findViewById(R.id.recurringCheckBox);
        Button setAlarmButton = findViewById(R.id.setAlarmButton);
        Button cancelAlarmButton = findViewById(R.id.cancelAlarmButton);

        setAlarmButton.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            boolean isRecurring = recurringCheckBox.isChecked();
            setAlarm(calendar, isRecurring);
        });

        cancelAlarmButton.setOnClickListener(v -> cancelAlarm());
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(Calendar calendar, boolean isRecurring) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Toast.makeText(this, "Failed to set/cancel alarm.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, AlarmReceiver.class);

        // Use FLAG_IMMUTABLE for a PendingIntent that doesn't need to be changed later
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PendingIntent.FLAG_IMMUTABLE
                : 0;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                flags
        );


        if (alarmManager != null) {
            if (isRecurring) {
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                );
                Toast.makeText(this, "Recurring alarm set for: " + calendar.getTime(), Toast.LENGTH_SHORT).show();
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
                Toast.makeText(this, "One-time alarm set for: " + calendar.getTime(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Toast.makeText(this, "Failed to set/cancel alarm.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, AlarmReceiver.class);

        // Use FLAG_IMMUTABLE for the PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        Toast.makeText(this, "Alarm canceled", Toast.LENGTH_SHORT).show();
    }

}
