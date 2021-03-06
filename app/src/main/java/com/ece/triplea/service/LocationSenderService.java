package com.ece.triplea.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ece.triplea.receiver.LocationSenderBroadcastReceiver;
import com.ece.triplea.R;
import com.ece.triplea.activity.ChildActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.DocumentTransform;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.support.v4.app.NotificationCompat.GROUP_ALERT_SUMMARY;

public class LocationSenderService extends Service {

    RequestQueue queue;
    LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    Context mContext;
    private Intent mIntent;
    boolean serviceSwitch;

    long mChildId, mUserId;
    String mChildName;

    Location lastReceivedLocation;

    public LocationSenderService(Context applicationContext) {
        super();
        mContext = applicationContext;
        Log.i("HERE", "here I am!");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("GLOBAL", MODE_PRIVATE);
        mChildId = sharedPreferences.getLong("child_id", -1);
        mChildName = sharedPreferences.getString("child_name", "");
        mUserId = sharedPreferences.getLong("user_id", -1);
        Intent intent = new Intent(getApplicationContext(), ChildActivity.class);
//        Intent intent = new Intent(this, Main2Activity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "UploadLocations")
                .setSmallIcon(R.drawable.icons8_location_96px)
                .setContentTitle("Uploading Your Location")
                .setContentText("Your location is being uploaded to the database")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
                .setGroup("My group")
                .setGroupSummary(false)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        startForeground(1, mBuilder.build());
    }

    public LocationSenderService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mIntent = intent;
        createLocationRequest();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
//                    tv.setText(location.getLatitude() + ", " + location.getLongitude());
//                    Toast.makeText(ChildActivity.this, "2", Toast.LENGTH_SHORT).show();
                    if (lastReceivedLocation == null || location.distanceTo(lastReceivedLocation)>1)
                        pushToDatabase(location);
                    lastReceivedLocation = location;
                }
            }
        };

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);




        startLocationUpdates();
        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent(this, LocationSenderBroadcastReceiver.class);

        sendBroadcast(broadcastIntent);
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  ");// + (counter++));
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setSmallestDisplacement(1);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);


        SettingsClient client = LocationServices.getSettingsClient(this);
        /*
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener((ChildActivity)mContext, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...

                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(mContext, "Permission is not granted", Toast.LENGTH_SHORT).show();

                    return;
                }

                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener((ChildActivity)mContext, new OnSuccessListener<MyLocation>() {
                            @Override
                            public void onSuccess(MyLocation location) {
                                // Got LAST known location. In some rare situations this can be null.
                                if (location != null) {
                                    // Logic to handle location object
                                    //tv.setText(location.getLatitude() + ", " + location.getLongitude());
//                                    Toast.makeText(ChildActivity.this, "1", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        /*task.addOnFailureListener((ChildActivity)mContext, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // MyLocation settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult((ChildActivity)mContext,
                                1);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
*/
    }

    private void pushToDatabase(final Location location) {
        if (queue == null) queue = Volley.newRequestQueue(this);
        String url = getString(R.string.base_url) +
                getString(R.string.ulr_location_add)
                + "?child_id=" + mChildId
                + "&family_id=" + mUserId
                + "&location_lat=" + location.getLatitude()
                + "&location_lng=" + location.getLongitude();

        // Request a string response from the provided URL.
        JsonArrayRequest stringRequest = new JsonArrayRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.v("onResponse", "\n" + "Response is: "+ response);
                        try {
                            JSONObject jsonObject = response.getJSONObject(0);
                            boolean error = jsonObject.getBoolean("error");
                            if (!error) {
                                long locationId = jsonObject.getLong("id");
                                String msg = jsonObject.getString("msg");
                                if (locationId < 0); //showSnackbar(msg);
                                else {
//                                    showSnackbar(msg);
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    final DocumentReference docRef = db
                                            .collection(String.valueOf(mUserId))
                                            .document(String.valueOf(mChildId));
                                    Map<String, Object> newLocation = new HashMap<>();
                                    newLocation.put("childName", mChildName);
                                    newLocation.put("lastLat", location.getLatitude());
                                    newLocation.put("lastLng", location.getLongitude());
                                    newLocation.put("locationId", locationId);
                                    newLocation.put("time", FieldValue.serverTimestamp());
                                    docRef.set(newLocation);
//                                    setLoading(false);
                                }
                            } else {
//                                showSnackbar("Cannot create account! try another username or phone number.");
//                                setLoading(false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
//                            showSnackbar("Error! " + e.getMessage());
//                            setLoading(false);
                        }

                        //changeState(STATE_TRACKING);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                //changeState(STATE_ERROR);
            }
        });

        // Add the request to the RequestQueue.

        queue.add(stringRequest);
    }



}