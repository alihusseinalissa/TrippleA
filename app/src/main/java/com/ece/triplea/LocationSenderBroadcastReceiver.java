package com.ece.triplea;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class LocationSenderBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LocationSenderBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        if (intent.getBooleanExtra("isTurnedOn", false)){
            LocationSenderService mService = new LocationSenderService(context);
            Intent mServiceIntent = new Intent(context, mService.getClass());
            context.startService(mServiceIntent);
        }

    }
}