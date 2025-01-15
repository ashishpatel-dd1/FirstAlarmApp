package com.example.simplealarmapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Show a toast message when the alarm is triggered
        Toast.makeText(context, "Alarm Triggered!", Toast.LENGTH_SHORT).show();

        // Play a notification sound
        MediaPlayer mediaPlayer = MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            // Release the MediaPlayer after the sound finishes
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
            });
        } else {
            Toast.makeText(context, "Unable to play alarm sound", Toast.LENGTH_SHORT).show();
        }
    }
}
