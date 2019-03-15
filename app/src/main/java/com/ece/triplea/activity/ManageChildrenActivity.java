package com.ece.triplea.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.ece.triplea.R;
import com.ece.triplea.model.Child;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ManageChildrenActivity extends AppCompatActivity implements Response.Listener<JSONArray>, Response.ErrorListener, View.OnClickListener, AdapterView.OnItemClickListener {

    final ArrayList<Child> mChildren = new ArrayList<>();
    ListView mListView;
    ChildrenListAdapter mAdapter;
    TextView txtNoChildren;
    ViewFlipper viewFlipper;
    RequestQueue volleyQueue;
    FloatingActionButton fab;
    Button btnNext;
    BottomSheetDialog mBottomSheetDialog;
    View sheetView;
    LinearLayout optionEdit, optionDelete;
    long selectedChildId = -1;
    long userId = -1;
    boolean initMode;
    private int PAGE_LOADING = 0;
    private int PAGE_ERROR = 1;
    private int PAGE_NO_CHILDREN = 2;
    private int PAGE_LIST = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_children);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
        userId = sharedPreferences.getLong("user_id", -1);
        initMode = sharedPreferences.getBoolean("init", true);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ManageChildrenActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_new_child, null);
                builder.setView(dialogView);
                builder.setTitle("Add New Child:");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                final EditText txtChildName = dialogView.findViewById(R.id.txtChildName);
                final EditText txtChildPhone = dialogView.findViewById(R.id.txtChildPhone);

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewFlipper.setDisplayedChild(PAGE_LOADING);
                        fab.hide();
                        mChildren.clear();




                        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                                getString(R.string.base_url) + "ChildrenAdd.php"
                                        + "?user_id=" + userId
                                        + "&child_name=" + txtChildName.getText().toString()
                                        + "&child_phone=" + txtChildPhone.getText().toString(),
                                null,
                                ManageChildrenActivity.this,
                                ManageChildrenActivity.this);
                        volleyQueue.add(request);

                    }
                });

                builder.show();


            }
        });

        viewFlipper = findViewById(R.id.manage_children_flipper);
        volleyQueue = Volley.newRequestQueue(this);


//        mChildren.add(new Child(0, "ali", "123"));
//        mChildren.add(new Child(1, "ahmed", "456"));
//        mChildren.add(new Child(2, "mohammad", "789"));



        mAdapter = new ChildrenListAdapter(mChildren);
        mListView = findViewById(R.id.listChildren);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();


        makeRequest();

        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);

        if (!initMode) {
            btnNext.setVisibility(View.GONE);
            CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM | Gravity.END;
            lp.setMargins(32, 32, 32, 32);
            fab.setLayoutParams(lp);
        }

        mListView.setOnItemClickListener(this);

        loadOptionsMenu();

        Button btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeRequest();
            }
        });


    }

    private void loadOptionsMenu() {
        mBottomSheetDialog = new BottomSheetDialog(this);
        sheetView = getLayoutInflater().inflate(R.layout.children_options, null);
        mBottomSheetDialog.setContentView(sheetView);

        optionEdit = sheetView.findViewById(R.id.option_edit);
        optionDelete = sheetView.findViewById(R.id.option_delete);

        optionEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Edit code here;
            }
        });

        optionDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(ManageChildrenActivity.this);
                builder.setMessage("Are you sure you want to delete this child?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewFlipper.setDisplayedChild(PAGE_LOADING);
                        fab.hide();
                        mChildren.clear();
                        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                                getString(R.string.base_url) + "ChildrenDelete.php"
                                        + "?user_id=" + userId
                                        + "&child_id=" + selectedChildId,
                                null,
                                ManageChildrenActivity.this,
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.v("error", "cannot delete");
                                    }
                                });
                        volleyQueue.add(request);
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();

            }
        });
    }

    private void makeRequest() {
        viewFlipper.setDisplayedChild(PAGE_LOADING);
        fab.hide();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getString(R.string.base_url) + "ChildrenGet.php?user_id=" + userId, null, this, this);
        volleyQueue.add(request);

    }

    @Override
    public void onResponse(JSONArray response) {
        try {
            JSONArray jsonArray = response.getJSONArray(0);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                long childId = jsonObject.getLong("child_id");
                String childName = jsonObject.getString("child_name");
                String childPhone = jsonObject.getString("child_phone");
                mChildren.add(new Child(childId, childName, childPhone));

            }
            mAdapter.notifyDataSetChanged();
            viewFlipper.setDisplayedChild(PAGE_LIST);
            fab.show();


        } catch (JSONException e) {
            e.printStackTrace();
            showError();
        }
    }

    private void showError() {
        viewFlipper.setDisplayedChild(PAGE_ERROR);
        fab.hide();
        Snackbar.make(mListView, "Please check your internet connection", Snackbar.LENGTH_LONG)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeRequest();
                    }
                }).show();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        showError();
    }

    @Override
    public void onClick(View v) {
        SharedPreferences sharedPreferences = getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("init", false);
        editor.apply();
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedChildId = id;
        mBottomSheetDialog.show();
    }

    public class ChildrenListAdapter extends BaseAdapter {

        ArrayList<Child> items = new ArrayList<>();

        ChildrenListAdapter(ArrayList<Child> list) {
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
            return items.get(position).getChildId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.child_row, null);

            TextView txtChildName = view.findViewById(R.id.txtChildName);
            TextView txtChildPhone = view.findViewById(R.id.txtChildPhone);
            ImageView imgChild = view.findViewById(R.id.imgChild);
            txtChildName.setText(items.get(position).getChildName());
            txtChildPhone.setText(items.get(position).getChildPhone());

            return view;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            if (mChildren.size()<=0) {
                viewFlipper.setDisplayedChild(PAGE_NO_CHILDREN);
                fab.show();
            } else {
                viewFlipper.setDisplayedChild(PAGE_LIST);
                fab.show();
            }
        }
    }

}
