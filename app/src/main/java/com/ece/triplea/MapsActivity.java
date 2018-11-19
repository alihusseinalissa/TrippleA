package com.ece.triplea;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    RequestQueue volleyQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        volleyQueue = Volley.newRequestQueue(this);
    }

    private void getLatestLocation(int childId) {
        String url = getString(R.string.base_url) +
                getString(R.string.ulr_location_get)
                + "?child_id=" + childId;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONArray jsonArray = (new JSONArray(response)).getJSONArray(0);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            double latitude =jsonObject.getDouble("location_lat");
                            double longitude = jsonObject.getDouble("location_lng");
                            String time = jsonObject.getString("location_time");
                            changeCurrentLocation(latitude, longitude, time);
                            getLatestLocation(1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });

        // Add the request to the RequestQueue.
        volleyQueue.add(stringRequest);
    }

    private void changeCurrentLocation(double newLatitude, double newLongitude, String newTime) {
        mMap.clear();
        LatLng newLocation = new LatLng(newLatitude, newLongitude);
        MarkerOptions marker = new MarkerOptions().position(newLocation).title("Your child's location" + ": " + newLatitude + ", " + newLongitude);
        mMap.addMarker(marker).showInfoWindow();
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

        getLatestLocation(1);
    }
}
