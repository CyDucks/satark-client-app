package org.cyducks.satark.core.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.cyducks.satark.core.conflictzone.model.ConflictZone;

import java.util.List;
import java.util.stream.Collectors;

public class GeofenceManager {
    private static final String TAG = "GeofenceManager";
    private final Context context;
    private final GeofencingClient geofencingClient;

    public GeofenceManager(Context context) {
        this.context = context;
        this.geofencingClient = LocationServices.getGeofencingClient(context);
    }

    public void setupGeofence(ConflictZone zone) {
        // Calculate center and radius that encompasses the yellow zone
        List<LatLng> yellowZonePoints = zone.getYellowZone()
                .stream()
                .map(geoPoint -> new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()))
                .collect(Collectors.toList());

        LatLngBounds bounds = getPolygonBounds(yellowZonePoints);
        LatLng center = bounds.getCenter();
        float radius = calculateRadius(bounds, center);

        Geofence geofence = new Geofence.Builder()
                .setRequestId(zone.getId())
                .setCircularRegion(
                        center.latitude,
                        center.longitude,
                        radius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        if (ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofence added successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to add geofence", e));
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }

    private LatLngBounds getPolygonBounds(List<LatLng> polygonPoints) {
        if (polygonPoints == null || polygonPoints.isEmpty()) {
            throw new IllegalArgumentException("Polygon points cannot be null or empty");
        }

        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double minLng = Double.POSITIVE_INFINITY;
        double maxLng = Double.NEGATIVE_INFINITY;

        // Find the minimum and maximum values
        for (LatLng point : polygonPoints) {
            minLat = Math.min(minLat, point.latitude);
            maxLat = Math.max(maxLat, point.latitude);
            minLng = Math.min(minLng, point.longitude);
            maxLng = Math.max(maxLng, point.longitude);
        }

        // Create bounds with southwest and northeast points
        return new LatLngBounds(
                new LatLng(minLat, minLng),  // Southwest corner
                new LatLng(maxLat, maxLng)   // Northeast corner
        );
    }

    /**
     * Calculates the radius needed to encompass the entire polygon.
     * Adds a buffer to ensure complete coverage.
     */
    private float calculateRadius(LatLngBounds bounds, LatLng center) {
        float[] results = new float[1];

        // Calculate distances to all corners
        float maxDistance = 0;
        LatLng[] corners = {
                new LatLng(bounds.southwest.latitude, bounds.southwest.longitude),
                new LatLng(bounds.southwest.latitude, bounds.northeast.longitude),
                new LatLng(bounds.northeast.latitude, bounds.southwest.longitude),
                new LatLng(bounds.northeast.latitude, bounds.northeast.longitude)
        };

        for (LatLng corner : corners) {
            Location.distanceBetween(
                    center.latitude, center.longitude,
                    corner.latitude, corner.longitude,
                    results
            );
            maxDistance = Math.max(maxDistance, results[0]);
        }

        // Add 10% buffer to ensure coverage
        return maxDistance * 1.1f;
    }
}
