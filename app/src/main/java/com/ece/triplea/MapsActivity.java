package com.ece.triplea;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nex3z.togglebuttongroup.MultiSelectToggleGroup;
import com.nex3z.togglebuttongroup.button.CircularToggle;
import com.nex3z.togglebuttongroup.button.LabelToggle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter, MultiSelectToggleGroup.OnCheckedStateChangeListener {

    private GoogleMap mMap;
    RequestQueue volleyQueue;
    private long userId;
    ArrayList<MyLocation> mLocations = new ArrayList<>();
    Map<Long, MyLocation> mapLocations = new HashMap<>();
    MultiSelectToggleGroup childrenPanel;
    ArrayList<Long> trackedChildren = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        volleyQueue = Volley.newRequestQueue(this);
        SharedPreferences sharedPreferences = getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
        userId = sharedPreferences.getLong("user_id", -1);
        childrenPanel = findViewById(R.id.children_panel);
        childrenPanel.setOnCheckedChangeListener(this);


    }

    private void getLatestLocation() {
        String url = getString(R.string.local_ip) +
                getString(R.string.ulr_location_get_last)
                + "?user_id=" + userId;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONArray jsonArray = (new JSONArray(response)).getJSONArray(0);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                long locationId = jsonObject.getLong("location_id");
                                long childId = jsonObject.getLong("childid");
                                String childName = jsonObject.getString("child_name");
                                double latitude = jsonObject.getDouble("location_lat");
                                double longitude = jsonObject.getDouble("location_lng");
                                String time = jsonObject.getString("location_time");
                                MyLocation location = new MyLocation(locationId, childId, childName, latitude, longitude, time);
                                mLocations.add(location);
                                mapLocations.put(childId, location);
                                addButton(childId, childName);
                                updateRealTimeLocations();
                            }

                            //getLatestLocation();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(MapsActivity.this, "error", Toast.LENGTH_SHORT).show();
                //getLatestLocation();
            }
        });

        // Add the request to the RequestQueue.
        volleyQueue.add(stringRequest);
    }

    private void addButton(long childId, String childName) {
        LabelToggle button = new LabelToggle(this);
        button.setText(childName);
        button.setTag(childId);
        button.setChecked(true);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        childrenPanel.addView(button, lp);
    }

    private void updateRealTimeLocations() {
        mMap.clear();
        LatLng newLocation = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < mLocations.size(); i++) {
            long childId = mLocations.get(i).getChildId();
            if (trackedChildren.contains(childId)) {
                double newLatitude = mLocations.get(i).getLatitude();
                double newLongitude = mLocations.get(i).getLongitude();
                newLocation = new LatLng(newLatitude, newLongitude);
                MarkerOptions marker = new MarkerOptions().position(newLocation)
                        .title(mLocations.get(i).getChildName() + "'s location" + ": " + newLatitude + ", " + newLongitude)
                        .snippet("Recorded at: " + mLocations.get(i).getTime());
                mMap.addMarker(marker).showInfoWindow();
                builder.include(marker.getPosition());
            }
        }
        if (trackedChildren.size() > 1) {
            LatLngBounds bounds = builder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 350));
        }
        else if (trackedChildren.size() == 1 && newLocation != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15));

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(this);
        getLatestLocation();
    }

    @Override
    public View getInfoWindow(Marker marker) {
//        return null;
        View view = getLayoutInflater().inflate(R.layout.marker_info_window,null);
        TextView tv1 = (TextView) view.findViewById(R.id.txtInfo1);
        TextView tv2 = (TextView) view.findViewById(R.id.txtInfo2);

        tv1.setText(marker.getTitle());
        tv2.setText(marker.getSnippet());



        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onCheckedStateChanged(MultiSelectToggleGroup group, int checkedId, boolean isChecked) {
        LabelToggle button = group.findViewById(checkedId);
        long childId = (long) button.getTag();
        if (isChecked) {
            trackedChildren.add(childId);
        } else {
            trackedChildren.remove(childId);
        }
        updateRealTimeLocations();
        Log.e("tag: " + button.getTag().toString(), "name: " + button.getText().toString() + ", " + "checked: " + isChecked);
    }
}
