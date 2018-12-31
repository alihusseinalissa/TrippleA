package com.ece.triplea;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.agrawalsuneet.loaderspack.loaders.MultipleRippleLoader;

public class ChildActivity extends AppCompatActivity {

    TextView tv;
    MultipleRippleLoader rippleLoader;
    ImageView errorImage;
    ViewFlipper view_flipper;
    final static int STATE_TRACKING = 0;
    final static int STATE_ERROR = 1;
    BroadcastReceiver broadcastReceiver;

    Intent mServiceIntent;
    private LocationSenderService mService;

    Context ctx;
    public Context getCtx() {
        return ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        view_flipper = (ViewFlipper)findViewById(R.id.view_flipper);
        View include1 = findViewById(R.id.uploading_layout);
        rippleLoader = include1.findViewById(R.id.ripple_layout);
        View include2 = findViewById(R.id.uploading_error_layout);
        errorImage = include2.findViewById(R.id.error_image);
        tv = findViewById(R.id.textView);
//        tv.setVisibility(View.GONE);

        ctx = this;
        mService = new LocationSenderService(getCtx());
        mServiceIntent = new Intent(getCtx(), mService.getClass());
        if (!isMyServiceRunning(mService.getClass())) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(mServiceIntent);
//            } else {
                startService(mServiceIntent);
//            }
        }



        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("AAACTION");
        intentFilter.setPriority(100);
        registerReceiver(broadcastReceiver, intentFilter);



        if (//ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            //    != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission is not granted!", Toast.LENGTH_SHORT).show();

/*
            new AlertDialog.Builder(this)
                    .setTitle("Permission is needed")
                    .setMessage("The application needs the permission of Accessing Fine Location, please accept.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                                    }, 1);
                        }
                    })

                    .setCancelable(false)
                    .show();

*/
        }


        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mServiceIntent.putExtra("isTurnedOn", false);
                stopService(mServiceIntent);
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


        Intent broadcastIntent = new Intent(this, LocationSenderBroadcastReceiver.class);
        broadcastIntent.putExtra("isTurnedOn", true);

        sendBroadcast(broadcastIntent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
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




}
