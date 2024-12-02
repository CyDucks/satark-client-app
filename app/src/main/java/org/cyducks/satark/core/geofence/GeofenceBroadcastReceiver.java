package org.cyducks.satark.core.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        
        if (geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "Error: " + geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        for (Geofence geofence : triggeringGeofences) {
            String zoneId = geofence.getRequestId();
            
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                // Start detailed monitoring service
                startZoneMonitoring(context, zoneId);
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                // Stop detailed monitoring service
                stopZoneMonitoring(context, zoneId);
            }
        }
    }

    private void startZoneMonitoring(Context context, String zoneId) {
        Intent serviceIntent = new Intent(context, ZoneMonitoringService.class);
        serviceIntent.putExtra("zoneId", zoneId);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    private void stopZoneMonitoring(Context context, String zoneId) {
        Intent serviceIntent = new Intent(context, ZoneMonitoringService.class);
        context.stopService(serviceIntent);
    }
}