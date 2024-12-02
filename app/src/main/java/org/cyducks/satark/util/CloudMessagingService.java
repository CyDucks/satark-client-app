package org.cyducks.satark.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.cyducks.satark.core.conflictzone.model.ConflictZone;
import org.cyducks.satark.core.geofence.GeofenceManager;
import org.cyducks.satark.core.geofence.ZoneCache;
import org.cyducks.satark.network.service.GeofenceApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CloudMessagingService extends FirebaseMessagingService {
    private static final String MASS_REPORT_EVENT = "org.cyducks.satark.MASS_REPORT_EVENT";
    private GeofenceApiService geofenceApiService;
    private GeofenceManager geofenceManager;
    private ZoneCache zoneCache;
    public CloudMessagingService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        geofenceApiService = retrofit.create(GeofenceApiService.class);
        geofenceManager = new GeofenceManager(this);
        zoneCache = new ZoneCache(this);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        SharedPreferences preferences = getSharedPreferences("fcm_token", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences
                .edit()
                .putString("token", token)
                .putString("timestamp", FieldValue.serverTimestamp().toString());

        editor.apply();
        editor.commit();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("CloudMessagingService", "onMessageReceived: " + message.getSenderId());
        Log.d("CloudMessagingService", "onMessageReceived: " + message.getData());
        if(message.getData().containsKey("event_type")) {
            Intent intent = new Intent(MASS_REPORT_EVENT);

            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

            broadcastManager.sendBroadcast(intent);
        }

        if(message.getData().containsKey("type")) {
            String type = message.getData().get("type");
            assert type != null;
            if(type.equals("NEW_ZONE")) {
                String zoneId = message.getData().get("zoneId");
                setupZone(zoneId);
            }
        }

    }

    private void setupZone(String zoneId) {
        geofenceApiService.getZoneById(zoneId)
                .enqueue(new Callback<ConflictZone>() {
                    @Override
                    public void onResponse(Call<ConflictZone> call, Response<ConflictZone> response) {
                        ConflictZone zone = response.body();
                        // Cache the zone
                        zoneCache.saveZone(zone);
                        // Setup geofence for the outer (yellow) zone
                        geofenceManager.setupGeofence(zone);
                    }

                    @Override
                    public void onFailure(Call<ConflictZone> call, Throwable throwable) {
                        Log.e("FCM", "Failed to fetch zone", throwable);
                    }
                });
    }
}