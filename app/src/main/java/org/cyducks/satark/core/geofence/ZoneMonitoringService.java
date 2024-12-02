package org.cyducks.satark.core.geofence;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.cyducks.satark.core.conflictzone.model.ConflictZone;

import java.util.List;
import java.util.stream.Collectors;

public class ZoneMonitoringService extends Service {
    private static final long MONITORING_INTERVAL = 30000; // 30 seconds
    private static final String CHANNEL_ID = "zone_monitoring";
    private static final int NOTIFICATION_ID = 1;

    private Handler handler;
    private ZoneCache zoneCache;
    private String monitoredZoneId;
    private FusedLocationProviderClient fusedLocationClient;
    private NotificationHelper notificationHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        zoneCache = new ZoneCache(this);
        notificationHelper = new NotificationHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startForeground(NOTIFICATION_ID, notificationHelper.createMonitoringNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            monitoredZoneId = intent.getStringExtra("zoneId");
            startMonitoring();
        }
        return START_STICKY;
    }

    private void startMonitoring() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                checkCurrentLocation();
                handler.postDelayed(this, MONITORING_INTERVAL);
            }
        });
    }

    private void checkCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        checkZoneContainment(location);
                    }
                });
        }
    }

    private void checkZoneContainment(Location location) {
        ConflictZone zone = zoneCache.getZone(monitoredZoneId);
        if (zone == null) return;

        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        String level = null;

        List<LatLng> redZone = zone.getRedZone().stream()
                .map(geoPoint -> new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()))
                .collect(Collectors.toList());
        List<LatLng> orangeZone = zone.getOrangeZone().stream()
                .map(geoPoint -> new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()))
                .collect(Collectors.toList());
        List<LatLng> yellowZone = zone.getYellowZone().stream()
                .map(geoPoint -> new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()))
                .collect(Collectors.toList());




        // Check from inner to outer
        if (isInZone(currentLocation, redZone)) {
            level = "RED";
        } else if (isInZone(currentLocation, orangeZone)) {
            level = "ORANGE";
        } else if (isInZone(currentLocation, yellowZone)) {
            level = "YELLOW";
        }

        if (level != null) {
            updateZoneStatus(zone, level);
        }
    }

    private boolean isInZone(LatLng point, List<LatLng> zonePoints) {
        return PolyUtil.containsLocation(point, zonePoints, true);
    }

    private void updateZoneStatus(ConflictZone conflictZone, String level) {
        // Show notification
        notificationHelper.showZoneNotification(level, conflictZone);
        
        // Update server if needed
        // ApiClient.getClient().create(ZoneApiService.class)...
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}