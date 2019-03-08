package com.ece.triplea.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.ece.triplea.service.LocationSenderService;

public class LocationSenderBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LocationSenderBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        if (pref.getBoolean("serviceSwitch", false)){
            LocationSenderService mService = new LocationSenderService(context);
            Intent mServiceIntent = new Intent(context, mService.getClass());
            context.startService(mServiceIntent);
        }

    }
}