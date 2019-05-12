package com.ece.triplea.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ece.triplea.R;
import com.ece.triplea.chat.Chatroom;
import com.ece.triplea.model.MyLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
    private long mUserId;
    ArrayList<MyLocation> mLatestLocations = new ArrayList<>();
    ArrayList<MyLocation> mHistoryLocations = new ArrayList<>();
    Map<Long, MyLocation> mapLocations = new HashMap<>();
    Map<Long, Bitmap> mapBitmaps = new HashMap<>();
    MultiSelectToggleGroup childrenPanel;
    ArrayList<Long> trackedChildren = new ArrayList<>();

    BottomSheetDialog mBottomSheetDialog;
    View sheetView;
    LinearLayout optionHistory, optionChat;
    long selectedChildId = -1;
    String selectedChildName = "";

    HistoryListAdapter mAdapter;
    ListView mListView;

    String lastSuccessfulResponse = "";


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
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        volleyQueue = Volley.newRequestQueue(this);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
        mUserId = sharedPreferences.getLong("user_id", -1);
        childrenPanel = findViewById(R.id.children_panel);
        childrenPanel.setOnCheckedChangeListener(this);
        childrenPanel.setOnLongClickListener(this);

        String url = getString(R.string.base_url) +
                getString(R.string.ulr_location_get_history)
                + "?user_id=" + mUserId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = (new JSONArray(response)).getJSONArray(0);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                long locationId = jsonObject.getLong("location_id");
                                final long childId = jsonObject.getLong("childid");
                                final String childName = jsonObject.getString("child_name");
                                double latitude = jsonObject.getDouble("location_lat");
                                double longitude = jsonObject.getDouble("location_lng");
                                String time = jsonObject.getString("location_time");
                                final MyLocation location = new MyLocation(locationId, childId, childName, latitude, longitude, time);
                                mHistoryLocations.add(location);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Display the first 500 characters of the response string.
                        mAdapter = new HistoryListAdapter(mHistoryLocations);
                        mListView.setAdapter(mAdapter);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(MapsActivity.this, "error", Toast.LENGTH_SHORT).show();
                //getLatestLocation();
            }
        });

        volleyQueue.add(stringRequest);

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
                        //TODO: use addMarkers() when bottom sheet is collapsed
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                mMap.clear();
//                double latitude = mLatestLocations.get(position).getLatitude();
//                double longitude = mLatestLocations.get(position).getLongitude();
//                LatLng point = new LatLng(latitude, longitude);
//                mMap.addMarker(new MarkerOptions().position(point));
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
//                mBottomSheetDialog.dismiss();
            }
        });

        final ImageView btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHistoryList();
            }
        });


    }

    private void showHistoryList() {
        final ImageView btnHistory = findViewById(R.id.btnHistory);
        CardView llBottomSheet = findViewById(R.id.bottom_sheet);

// init the bottom sheet behavior
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

// change the state of the bottom sheet
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    btnHistory.setImageResource(R.drawable.ic_keyboard_arrow_down_black_32dp);
                } else
                    btnHistory.setImageResource(R.drawable.ic_history_black_32dp);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void getLatestLocation() {
        String url = getString(R.string.base_url) +
                getString(R.string.ulr_location_get_last)
                + "?user_id=" + mUserId;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        lastSuccessfulResponse = response;
                        addMarkers();

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

    void addMarkers() {
        try {
            JSONArray jsonArray = (new JSONArray(lastSuccessfulResponse)).getJSONArray(0);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                long locationId = jsonObject.getLong("location_id");
                final long childId = jsonObject.getLong("childid");
                final String childImage = jsonObject.getString("child_image");
                final String childName = jsonObject.getString("child_name");
                double latitude = jsonObject.getDouble("location_lat");
                double longitude = jsonObject.getDouble("location_lng");
                String time = jsonObject.getString("location_time");
                final MyLocation location = new MyLocation(locationId, childId, childName, latitude, longitude, time);
                mLatestLocations.add(location);
                mapLocations.put(childId, location);
                mHistoryLocations.add(location);
                if (mAdapter != null) mAdapter.notifyDataSetChanged();
                Glide.with(this)
                        .load(getChildImageUrl(childImage))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        mapBitmaps.put(childId, convertToBitmap(resource, 300, 300));
                        addButton(childId, childName);
                        updateRealTimeLocations();

                    }
                });

            }

            //getLatestLocation();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        return mutableBitmap;
    }

    private String getChildImageUrl(String childImage) {
        return getString(R.string.images_url) + childImage;
    }

    private void addButton(long childId, final String childName) {
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
                mBottomSheetDialog.show();
                selectedChildId = childId;
                selectedChildName = childName;
                return true;
            }
        });
        childrenPanel.addView(button, lp);
    }

    private void updateRealTimeLocations() {
        mMap.clear();
        LatLng newLocation = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < mLatestLocations.size(); i++) {
            long childId = mLatestLocations.get(i).getChildId();
            if (trackedChildren.contains(childId)) {
                double newLatitude = mLatestLocations.get(i).getLatitude();
                double newLongitude = mLatestLocations.get(i).getLongitude();
                newLocation = new LatLng(newLatitude, newLongitude);
                MarkerOptions marker = new MarkerOptions().position(newLocation)
                        .title(mLatestLocations.get(i).getChildName() + "'s location" + ": " + newLatitude + ", " + newLongitude)
                        .snippet("Recorded at: " + mLatestLocations.get(i).getTime());
                addMarker(marker, childId);

                builder.include(marker.getPosition());
            }
        }
        if (trackedChildren.size() > 1) {
            LatLngBounds bounds = builder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 350));
        } else if (trackedChildren.size() == 1 && newLocation != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateRealTimeLocations();
            }
        }, 3000);

    }

    private void addMarker(MarkerOptions marker, long childId) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view1 = inflater.inflate(R.layout.marker_icon, null);

        ImageView imgMarker = (ImageView) view1.findViewById(R.id.MarkerIcon);
        //imgMarker.setImageResource(myColors.getIconColor(color));

        ImageView imgImage = (ImageView) view1.findViewById(R.id.MarkerImage);

        Bitmap childImage = mapBitmaps.get(childId);

        if (childImage != null) {

            Bitmap ThumbImage;
            try {
                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri2);
                ThumbImage = ThumbnailUtils.extractThumbnail(childImage,
                        300, 300);
                imgImage.setImageBitmap(ThumbImage);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        Bitmap bitmap = getBitmapFromView(view1);
        marker.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        mMap.addMarker(marker).showInfoWindow();
    }

    public static Bitmap getBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);
        return bitmap;
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
        View view = getLayoutInflater().inflate(R.layout.marker_info_window, null);
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
        optionChat = sheetView.findViewById(R.id.option_chat);

        optionHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHistoryList();
                mBottomSheetDialog.dismiss();
            }
        });

        optionChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sender = Long.toString(selectedChildId);
                String room = Long.toString(mUserId);
                Intent intent = new Intent(MapsActivity.this, Chatroom.class);
                intent.putExtra("Name", "Your Father");
                intent.putExtra("chatroom",room + "-" + sender);
                intent.putExtra("title", selectedChildName);
                startActivity(intent);
                mBottomSheetDialog.dismiss();
            }
        });
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
