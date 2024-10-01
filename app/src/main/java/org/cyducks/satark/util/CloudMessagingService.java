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

public class CloudMessagingService extends FirebaseMessagingService {
    public CloudMessagingService() {
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
        Intent intent = new Intent("mass_report_receipt");

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        broadcastManager.sendBroadcast(intent);
    }
}