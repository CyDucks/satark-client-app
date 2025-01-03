package org.cyducks.satark.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LocationUtil {
    private final Activity context;
    private final FusedLocationProviderClient locationProviderClient;
    private LocationListener locationListener;


    public interface LocationListener {
        void onResult(Location location);
        void onError(String exception);
    }

    public LocationUtil(Activity context) {
        this.context = context;
        locationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void getCurrentLocation(LocationListener locationListener) {
        this.locationListener = locationListener;

        if(checkPermissions()) {
            if(isLocationEnabled()) {
                requestCurrentLocation();
            } else {
                locationListener.onError("Please enable location services");
                showLocationSettingsDialog();
            }
        } else {
            requestPermissions();
        }
    }

    public void getLastLocation(LocationListener locationListener) {
        this.locationListener = locationListener;

        if(checkPermissions()) {
            if(isLocationEnabled()) {
                requestLastLocation();
            } else {
                locationListener.onError("Please enable location services");
                showLocationSettingsDialog();
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestCurrentLocation() {
        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        locationProviderClient.getCurrentLocation(request, new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        }).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                locationListener.onResult(task.getResult());
            } else {
                locationListener.onError(task.getException().getMessage());
            }
        });
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(context,
                new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 1001);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showLocationSettingsDialog() {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Location Services Required")
                .setMessage("Please enable location services to use this feature")
                .setPositiveButton("Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @SuppressLint("MissingPermission")
    private void requestLastLocation() {
        locationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                locationListener.onResult(task.getResult());
            } else {
                locationListener.onError(task.getException().getMessage());
            }
        });
    }
}
