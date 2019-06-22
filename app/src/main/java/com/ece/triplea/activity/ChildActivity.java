package com.ece.triplea.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.agrawalsuneet.loaderspack.loaders.MultipleRippleLoader;
import com.bitvale.switcher.SwitcherC;
import com.bitvale.switcher.SwitcherX;
import com.ece.triplea.chat.Chatroom;
import com.ece.triplea.service.LocationSenderService;
import com.ece.triplea.R;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ChildActivity extends AppCompatActivity {

    SwitcherC serviceSwitch;
    MultipleRippleLoader rippleLoader;
    ImageView errorImage;
    ViewFlipper view_flipper;
    final static int STATE_TRACKING = 0;
    final static int STATE_ERROR = 1;
    BroadcastReceiver broadcastReceiver;
    SharedPreferences pref;

    Intent mServiceIntent;
    private LocationSenderService mService;

    Context ctx;

    long mChildId;
    long mparent;

    public Context getCtx() {
        return ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        if (ContextCompat.checkSelfPermission(ChildActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(ChildActivity.this)
                    .setTitle("Permission is needed")
                    .setMessage("The application needs the permission of Accessing Fine Location, please accept.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(ChildActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                                    }, 0);
                        }
                    })

                    .setCancelable(false)
                    .show();
        }

        getSupportActionBar().setTitle("");

        pref = getApplicationContext().getSharedPreferences("GLOBAL", MODE_PRIVATE);
        mChildId = pref.getLong("child_id", -1);
        mparent = pref.getLong("user_id", -1);
        view_flipper = (ViewFlipper) findViewById(R.id.view_flipper);
        View include1 = findViewById(R.id.uploading_layout);
        rippleLoader = include1.findViewById(R.id.ripple_layout);
        View include2 = findViewById(R.id.uploading_error_layout);
        errorImage = include2.findViewById(R.id.error_image);
        serviceSwitch = findViewById(R.id.serviceSwitch);

        ctx = this;
        mService = new LocationSenderService(getCtx());
        mServiceIntent = new Intent(getCtx(), mService.getClass());

        boolean running = isMyServiceRunning(mService.getClass());
        serviceSwitch.setChecked(running, true);
        rippleLoader.setVisibility(running ? View.VISIBLE : View.INVISIBLE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "Broadcast Received!!", Toast.LENGTH_SHORT).show();
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("AAACTION");
        intentFilter.setPriority(100);
        registerReceiver(broadcastReceiver, intentFilter);


        serviceSwitch.setOnCheckedChangeListener(new Function1<Boolean, Unit>() {
            @Override
            public Unit invoke(Boolean isChecked) {
                if (isChecked) {

                    if (!isMyServiceRunning(mService.getClass())) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                       startForegroundService(mServiceIntent);
//                       } else {
                        startService(mServiceIntent);
//                    }
                    }

                } else {
                    stopService(mServiceIntent);
                }

                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("serviceSwitch", isChecked);
                editor.apply();

                rippleLoader.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                return null;
            }
        });


//        Switch trackingServiceSwitch = findViewById(R.id.tracking_service_switch);
//        trackingServiceSwitch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (((Switch)v).isChecked()){
//
//                }
//                else {
//
//                }
//            }
//        });


//        Intent broadcastIntent = new Intent(this, LocationSenderBroadcastReceiver.class);
//        SharedPreferences.Editor editor = pref.edit();
//        editor.putBoolean("serviceSwitch", false);
//        editor.commit();
//        sendBroadcast(broadcastIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.menu_manage_children).setVisible(false);
        menu.findItem(R.id.option_history).setVisible(false);
        menu.findItem(R.id.menu_map_type).setVisible(false);
        menu.findItem(R.id.menu_tracking_type).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign_out:
                if (isMyServiceRunning(mService.getClass()))
                    stopService(mServiceIntent);
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("init", true);
                editor.putLong("user_id", -1);
                editor.putLong("child_id", -1);
                editor.putString("mode", "undefined");
                editor.apply();
                Intent intent = new Intent(this, StepperActivity.class);
                startActivity(intent);
                this.finish();
                return true;
            case R.id.option_chat:
                String room = Long.toString(mparent);
                String sender = Long.toString(mChildId);
                Intent intent2 = new Intent(ChildActivity.this, Chatroom.class);
                intent2.putExtra("mode", "child");
                intent2.putExtra("childName", "childName");
                intent2.putExtra("userName", "UserName");
                intent2.putExtra("chatroom", room + "-" + sender);
                intent2.putExtra("title", "Your parent");
                startActivity(intent2);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        //stopService(mServiceIntent);
        super.onDestroy();
    }


    private void changeState(int newState) {
        view_flipper.setDisplayedChild(newState);
    }

//    public class GsonRequest<T> extends Request<T> {
//        private final Gson gson = new Gson();
//        private final Class<T> clazz;
//        private final Map<String, String> headers;
//        private final Response.Listener<T> listener;
//
//        /**
//         * Make a GET request and return a parsed object from JSON.
//         *
//         * @param url URL of the request to make
//         * @param clazz Relevant class object, for Gson's reflection
//         * @param headers Map of request headers
//         */
//        public GsonRequest(String url, Class<T> clazz, Map<String, String> headers,
//                           Response.Listener<T> listener, Response.ErrorListener errorListener) {
//            super(Method.POST, url, errorListener);
//            this.clazz = clazz;
//            this.headers = headers;
//            this.listener = listener;
//        }
//
//        @Override
//        public Map<String, String> getHeaders() throws AuthFailureError {
//            return headers != null ? headers : super.getHeaders();
//        }
//
//        @Override
//        protected void deliverResponse(T response) {
//            listener.onResponse(response);
//        }
//
//        @Override
//        protected Response<T> parseNetworkResponse(NetworkResponse response) {
//            try {
//                String json = new String(
//                        response.data,
//                        HttpHeaderParser.parseCharset(response.headers));
//                return Response.success(
//                        gson.fromJson(json, clazz),
//                        HttpHeaderParser.parseCacheHeaders(response));
//            } catch (UnsupportedEncodingException e) {
//                return Response.error(new ParseError(e));
//            } catch (JsonSyntaxException e) {
//                return Response.error(new ParseError(e));
//            }
//        }
//    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


}
