package org.cyducks.satark.core.geofence;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.cyducks.satark.MainActivity;
import org.cyducks.satark.R;
import org.cyducks.satark.core.conflictzone.model.ConflictZone;

import java.util.Arrays;

public class NotificationHelper {
    private static final String ZONE_CHANNEL_ID = "zone_alerts";
    private static final String MONITORING_CHANNEL_ID = "zone_monitoring";
    private static final int ALERT_NOTIFICATION_ID = 1;
    private static final int MONITORING_NOTIFICATION_ID = 2;

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Alert Channel
            NotificationChannel alertChannel = new NotificationChannel(
                    ZONE_CHANNEL_ID,
                    "Zone Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            alertChannel.setDescription("Alerts for zone transitions");
            alertChannel.enableVibration(true);
            alertChannel.setVibrationPattern(new long[]{0, 500, 250, 500});

            // Monitoring Channel
            NotificationChannel monitorChannel = new NotificationChannel(
                    MONITORING_CHANNEL_ID,
                    "Zone Monitoring",
                    NotificationManager.IMPORTANCE_LOW
            );
            monitorChannel.setDescription("Ongoing zone monitoring status");
            monitorChannel.setShowBadge(false);

            notificationManager.createNotificationChannels(
                    Arrays.asList(alertChannel, monitorChannel)
            );
        }
    }

    public Notification createMonitoringNotification() {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(context, MONITORING_CHANNEL_ID)
                .setContentTitle("Zone Monitoring Active")
                .setContentText("Monitoring nearby zones")
                .setSmallIcon(R.drawable.lock_icon)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    public void showZoneNotification(String level, ConflictZone zone) {
        // Get notification style based on level
        NotificationStyle style = getNotificationStyle(level);

        // Create notification intent
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("zone_id", zone.getId());
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ZONE_CHANNEL_ID)
                .setContentTitle(style.title)
                .setContentText(style.message)
                .setSmallIcon(style.icon)
                .setColor(style.color)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        // Add actions based on level
        if ("RED".equals(level)) {
            // Add emergency action
            Intent emergencyIntent = new Intent(context, StopMonitoringReceiver.class);
            PendingIntent emergencyPendingIntent = PendingIntent.getBroadcast(
                    context, 0, emergencyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.addAction(
                    R.drawable.lock_icon,
                    "Okay, Got it",
                    emergencyPendingIntent
            );
        }

        notificationManager.notify(ALERT_NOTIFICATION_ID, builder.build());
    }

    private NotificationStyle getNotificationStyle(String level) {
        switch (level) {
            case "RED":
                return new NotificationStyle(
                        "High Risk Zone Alert!",
                        "You are in a high-risk area. Please exercise extreme caution.",
                        R.drawable.edit_icon,
                        Color.RED
                );
            case "ORANGE":
                return new NotificationStyle(
                        "Warning: Medium Risk Zone",
                        "You are approaching a high-risk area.",
                        R.drawable.lock_icon,
                        Color.parseColor("#FF8C00")
                );
            case "YELLOW":
                return new NotificationStyle(
                        "Caution: Monitored Zone",
                        "You have entered a monitored area.",
                        R.drawable.email_icon,
                        Color.YELLOW
                );
            default:
                return new NotificationStyle(
                        "Zone Update",
                        "Zone status has changed.",
                        R.drawable.phone_icon,
                        Color.BLUE
                );
        }
    }

    private static class NotificationStyle {
        final String title;
        final String message;
        final int icon;
        final int color;

        NotificationStyle(String title, String message, int icon, int color) {
            this.title = title;
            this.message = message;
            this.icon = icon;
            this.color = color;
        }
    }
}