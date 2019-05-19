package com.ece.triplea.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.ArrayMap;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
    CardView llBottomSheet;
    RequestQueue volleyQueue;
    private long mUserId;
    ArrayList<MyLocation> mLatestLocations = new ArrayList<>();
    ArrayList<MyLocation> mHistoryLocations = new ArrayList<>();
    Map<Long, MyLocation> mapLocations = new HashMap<>();
    Map<Long, Bitmap> mapBitmaps = new HashMap<>();
    Map<Long, Marker> lastAddedMarkers = new HashMap<>();
    MultiSelectToggleGroup childrenPanel;
    ArrayList<Long> trackedChildren = new ArrayList<>();
    ViewFlipper viewFlipper;
    Button btnHistoryTryAgain;
    private final static int flipperList = 0;
    private final static int flipperError = 1;
    private final static int flipperWait = 2;

    BottomSheetDialog mBottomSheetDialog;
    View sheetView;
    LinearLayout optionHistory, optionChat;
    long selectedChildId = -1;
    String selectedChildName = "";

    HistoryListAdapter mAdapter;
    ListView mListView;

    String lastSuccessfulResponse = "";
    ZoomType mZoomType = ZoomType.ALL;

    Map<Long, ArrayList<Marker>> mMarkers = new HashMap<>();
    private Map<Long, LabelToggle> mButtons = new HashMap<>();
    //    private boolean mZoomEnabled = true;
    private Marker lastHistoryMarker;

    enum ZoomType {
        NONE,
        ALL,
        LAST
    }

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
                return true;
            case R.id.mapNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                item.setChecked(true);
                return true;
            case R.id.mapSat:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                item.setChecked(true);
                return true;
            case R.id.mapTer:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                item.setChecked(true);
                return true;
            case R.id.mapHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                item.setChecked(true);
                return true;
            case R.id.mapNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                item.setChecked(true);
                return true;
            case R.id.zoomType1:
                this.mZoomType = ZoomType.NONE;
                item.setChecked(true);
                return true;
            case R.id.zoomType2:
                moveCameraToBounds();
                this.mZoomType = ZoomType.ALL;
                item.setChecked(true);
                return true;
            case R.id.zoomType3:
                // TODO: zoom to the latest location (marker)
                this.mZoomType = ZoomType.LAST;
                item.setChecked(true);
                return true;
//            case R.id.zoom:
//                this.mZoomEnabled = item.isChecked();
////                item.setChecked(!item.isChecked());
//                return true;
            case R.id.option_history:
                showHistoryList();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showHistoryList() {
        //final ImageView btnHistory = (ImageView) findViewById(R.id.btnHistory);
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

                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    if (lastHistoryMarker != null)
                        lastHistoryMarker.remove();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        volleyQueue = Volley.newRequestQueue(this);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
        mUserId = sharedPreferences.getLong("user_id", -1);
        childrenPanel = findViewById(R.id.children_panel);
        childrenPanel.setOnCheckedChangeListener(this);
        childrenPanel.setOnLongClickListener(this);
        llBottomSheet = findViewById(R.id.bottom_sheet);
        viewFlipper = findViewById(R.id.flipper);
        btnHistoryTryAgain = findViewById(R.id.btnHistoryTryAgain);
        btnHistoryTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getHistory();
            }
        });


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
                //mMap.clear();
                if (lastHistoryMarker != null)
                    lastHistoryMarker.remove();
                MyLocation location = mHistoryLocations.get(position);
                lastHistoryMarker = addMarker(location);
                lastHistoryMarker.showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(lastHistoryMarker.getPosition()));
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        loadChildrenImages();


    }

    private void loadChildrenImages() {
        Log.v("MapsActivity", "Begin loading children images");
        String url = getString(R.string.base_url) +
                getString(R.string.ulr_children_get)
                + "?user_id=" + mUserId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray(0);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                final long childId = jsonObject.getLong("child_id");
                                String childName = jsonObject.getString("child_name");
                                String childImage = jsonObject.getString("child_image");
                                mMarkers.put(childId, new ArrayList<Marker>());
                                mapBitmaps.put(childId, BitmapFactory.decodeResource(getResources(),
                                        R.mipmap.ic_launcher));
                                addButton(childId, childName);
                                Glide.with(MapsActivity.this)
                                        .load(getChildImageUrl(childImage))
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(new SimpleTarget<Drawable>() {
                                            @Override
                                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                mapBitmaps.put(childId, convertToBitmap(resource, 300, 300));
                                                if (mMarkers.get(childId) != null) {
                                                    for (Marker marker : mMarkers.get(childId))
                                                        marker.setIcon(getChildMarkerIcon(childId));
                                                }
                                            }
                                        });
                            }
                            final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                    .findFragmentById(R.id.map);
                            mapFragment.getMapAsync(MapsActivity.this);
                            Log.v("MapsActivity", "Children images loaded successfully");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        mAdapter = new HistoryListAdapter(mHistoryLocations);
//                        mListView.setAdapter(mAdapter);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("MapsActivity", "Children images loading failed");
                Log.e("MapsActivity", error.getMessage());


                //getLatestLocation();
            }
        });

        volleyQueue.add(request);


    }

    private BitmapDescriptor getChildMarkerIcon(long childId) {
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
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void getHistory() {
        viewFlipper.setDisplayedChild(flipperWait);
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
                        viewFlipper.setDisplayedChild(flipperList);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(MapsActivity.this, "error", Toast.LENGTH_SHORT).show();
                viewFlipper.setDisplayedChild(flipperError);
            }
        });

        volleyQueue.add(stringRequest);

    }

    private String getChildImageUrl(String childImage) {
        return getString(R.string.images_url) + childImage;
    }

    public Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        return mutableBitmap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(33, 44), 15));
        getLatestFromFirebase();
        getHistory();
        //getLatestLocation();
    }

    private void getLatestFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final int[] n = {0}; //counter for received locations
        for (final long childId : mapBitmaps.keySet()) {
            final DocumentReference docRef = db
                    .collection(String.valueOf(mUserId))
                    .document(String.valueOf(childId));
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("Firestore", "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        n[0]++;
                        long locationId = snapshot.getLong("locationId");
                        String childName = snapshot.getString("childName");
                        double lat = snapshot.getDouble("lastLat");
                        double lng = snapshot.getDouble("lastLng");
                        String time = snapshot.getTimestamp("time").toDate().toString();
                        MyLocation receivedLocation = new MyLocation(locationId, childId, childName, lat, lng, time);
                        if (mButtons.get(childId).isChecked())
                            addMarker(receivedLocation);

                        Log.d("Firestore", "Current data: " + snapshot.getData());
                    } else {
                        Log.d("Firestore", "Current data: null");
                    }
                }
            });
        }

    }

    private void getLatestFromFirebaseOnce(final long childId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db
                .collection(String.valueOf(mUserId))
                .document(String.valueOf(childId));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        long locationId = snapshot.getLong("locationId");
                        String childName = snapshot.getString("childName");
                        double lat = snapshot.getDouble("lastLat");
                        double lng = snapshot.getDouble("lastLng");
                        String time = snapshot.getTimestamp("time").toDate().toString();
                        MyLocation receivedLocation = new MyLocation(locationId, childId, childName, lat, lng, time);
                        addMarker(receivedLocation);

                        Log.d("Firestore", "Current data: " + snapshot.getData());
                    }
                } else {
                    Log.w("Firestore", "Listen failed.");
                }

            }

        });

    }

    private void moveCameraToBounds() {

        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : lastAddedMarkers.values()) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 350));
    }

    @Override
    public void onBackPressed() {
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else super.onBackPressed();
    }

    private Marker addMarker(MyLocation newLocation) {
        long childId = newLocation.getChildId();
        double lat = newLocation.getLatitude();
        double lng = newLocation.getLongitude();
        LatLng point = new LatLng(lat, lng);
        MarkerOptions marker = new MarkerOptions().position(point)
                .title(newLocation.getChildName() + "'s location" + ": " + lat + ", " + lng)
                .snippet("Recorded at: " + newLocation.getTime());
        Marker addedMarker = drawMarkerOnMap(newLocation.getChildId(), marker);
        ArrayList<Marker> markers = mMarkers.get(childId);
        if (markers != null) {
            markers.add(addedMarker);
            mMarkers.put(childId, markers);
        }
        lastAddedMarkers.put(childId, addedMarker);
        if (mZoomType == ZoomType.ALL) { // if option enabled for tracking ALL children at the same time
            moveCameraToBounds();
        } else if (mZoomType == ZoomType.LAST) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
        }
        return addedMarker;
    }

    private Marker drawMarkerOnMap(long childId, MarkerOptions marker) {
        marker.icon(getChildMarkerIcon(childId));
        return mMap.addMarker(marker);
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


    @Override
    public void onCheckedStateChanged(MultiSelectToggleGroup group, int checkedId, boolean isChecked) {
        LabelToggle button = group.findViewById(checkedId);
        long childId = (long) button.getTag();
        if (isChecked) {
            //trackedChildren.add(childId);
            getLatestFromFirebaseOnce(childId);
        } else {
            //trackedChildren.remove(childId);
            try {
                for (Marker marker :
                        mMarkers.get(childId)) {
                    marker.remove();
                }
                //lastAddedMarkers.get(childId).remove();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // remove marker from markers map
        Log.e("tag: " + button.getTag().toString(), "name: " + button.getText().toString() + ", " + "checked: " + isChecked);
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
        mButtons.put(childId, button);
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
                intent.putExtra("chatroom", room + "-" + sender);
                intent.putExtra("title", selectedChildName);
                startActivity(intent);
                mBottomSheetDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
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
        }
    }

}
