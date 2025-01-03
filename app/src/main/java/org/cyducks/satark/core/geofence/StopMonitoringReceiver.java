package org.cyducks.satark.core.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopMonitoringReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
       if(intent != null && intent.getAction().equals("STOP_MONITORING")) {
            context.stopService(new Intent(context, ZoneMonitoringService.class));
       }
    }
}