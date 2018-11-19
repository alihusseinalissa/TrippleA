package com.ece.triplea;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.agrawalsuneet.loaderspack.loaders.MultipleRippleLoader;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class ChildActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    TextView tv;
    LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    MultipleRippleLoader rippleLoader;
    ImageView errorImage;
    ViewFlipper view_flipper;
    final static int STATE_TRACKING = 0;
    final static int STATE_ERROR = 1;

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
        tv.setVisibility(View.GONE);

        createLocationRequest();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
//                    tv.setText(location.getLatitude() + ", " + location.getLongitude());
//                    Toast.makeText(ChildActivity.this, "2", Toast.LENGTH_SHORT).show();
                    pushToDatabase(location);
                }
            }
        };


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


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


    }

    private void pushToDatabase(Location location) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getString(R.string.base_url) +
                getString(R.string.ulr_location_add)
                + "?child_id=" + "1"
                + "&family_id=" + "1"
                + "&location_lat=" + location.getLatitude()
                + "&location_lng=" + location.getLongitude();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        tv.append("\n" + "Response is: "+ response);
                        changeState(STATE_TRACKING);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                changeState(STATE_ERROR);
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
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


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);


        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                if (ActivityCompat.checkSelfPermission(ChildActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ChildActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(ChildActivity.this, "Permission is not granted", Toast.LENGTH_SHORT).show();

                    return;
                }
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(ChildActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    // Logic to handle location object
                                    tv.setText(location.getLatitude() + ", " + location.getLongitude());
//                                    Toast.makeText(ChildActivity.this, "1", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(ChildActivity.this,
                                1);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }



}
