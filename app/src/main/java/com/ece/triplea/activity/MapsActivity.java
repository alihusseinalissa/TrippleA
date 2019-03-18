package com.ece.triplea.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ece.triplea.R;
import com.ece.triplea.model.MyLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nex3z.togglebuttongroup.MultiSelectToggleGroup;
import com.nex3z.togglebuttongroup.button.LabelToggle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter, MultiSelectToggleGroup.OnCheckedStateChangeListener, View.OnLongClickListener {

    private GoogleMap mMap;
    RequestQueue volleyQueue;
    private long userId;
    ArrayList<MyLocation> mLocations = new ArrayList<>();
    Map<Long, MyLocation> mapLocations = new HashMap<>();
    MultiSelectToggleGroup childrenPanel;
    ArrayList<Long> trackedChildren = new ArrayList<>();

    BottomSheetDialog mBottomSheetDialog;
    View sheetView;
    LinearLayout optionHistory, optionDelete;
    long selectedChildId = -1;

    HistoryListAdapter mAdapter;
    ListView mListView;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign_out:
                SharedPreferences sharedPreferences = getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("init", true);
                editor.putLong("user_id", -1);
                editor.apply();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                this.finish();
                return true;
            case R.id.menu_manage_children:
                Intent intent2 = new Intent(this, ManageChildrenActivity.class);
                startActivity(intent2);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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
        childrenPanel.setOnLongClickListener(this);

        loadOptionsMenu();

        mListView = findViewById(R.id.listHistory);
        mListView.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow NestedScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow NestedScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        final ImageView btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardView llBottomSheet = findViewById(R.id.bottom_sheet);

// init the bottom sheet behavior
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

// change the state of the bottom sheet
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

                bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        if (newState == BottomSheetBehavior.STATE_EXPANDED){
                            btnHistory.setImageResource(R.drawable.ic_keyboard_arrow_down_black_32dp);
                        }
                        else
                            btnHistory.setImageResource(R.drawable.ic_history_black_32dp);
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                    }
                });
            }
        });


    }

    private void getLatestLocation() {
        String url = getString(R.string.base_url) +
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
                                mAdapter = new HistoryListAdapter(mLocations);
                                mListView.setAdapter(mAdapter);
                                mAdapter.notifyDataSetChanged();
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

        button.setLongClickable(true);
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                long childId = (long) v.getTag();
                selectedChildId = childId;
                mBottomSheetDialog.show();
                return true;
            }
        });
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

    @Override
    public boolean onLongClick(View v) {
        long childId = (long) v.getTag();
        selectedChildId = childId;
        mBottomSheetDialog.show();
        return false;
    }

    private void loadOptionsMenu() {
        mBottomSheetDialog = new BottomSheetDialog(this);
        sheetView = getLayoutInflater().inflate(R.layout.map_child_options, null);
        mBottomSheetDialog.setContentView(sheetView);

        optionHistory = sheetView.findViewById(R.id.option_history);
        //optionDelete = sheetView.findViewById(R.id.option_delete);

        optionHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("clicked", String.valueOf(selectedChildId));
            }
        });

//        optionDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mBottomSheetDialog.dismiss();
//
//            }
//        });
    }

    public class HistoryListAdapter extends BaseAdapter {

        ArrayList<MyLocation> items = new ArrayList<>();

        HistoryListAdapter(ArrayList<MyLocation> list) {
            this.items = list;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).getLocationId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.history_row, null);

            TextView txtChildName = view.findViewById(R.id.txtChildName);
            TextView txtChildLocation = view.findViewById(R.id.txtChildLocation);
            TextView txtTime = view.findViewById(R.id.txtTime);
            txtChildName.setText(items.get(position).getChildName());
            txtChildLocation.setText(String.format("%s, %s", items.get(position).getLatitude(), items.get(position).getLongitude()));
            txtTime.setText(items.get(position).getTime());

            return view;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
//            if (mChildren.size()<=0) {
//                viewFlipper.setDisplayedChild(PAGE_NO_CHILDREN);
//                fab.show();
//            } else {
//                viewFlipper.setDisplayedChild(PAGE_LIST);
//                fab.show();
//            }
        }
    }

}
