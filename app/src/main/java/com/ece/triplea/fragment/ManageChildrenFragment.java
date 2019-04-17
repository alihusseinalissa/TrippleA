package com.ece.triplea.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ece.triplea.R;
import com.ece.triplea.activity.ChildActivity;
import com.ece.triplea.activity.MapsActivity;
import com.ece.triplea.activity.StepperActivity;
import com.ece.triplea.model.Child;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ManageChildrenFragment extends Fragment implements Response.Listener<JSONArray>, Response.ErrorListener, AdapterView.OnItemClickListener, BlockingStep, AdapterView.OnItemLongClickListener {

    ArrayList<Child> mChildren;
    ListView mListView;
    ChildrenListAdapter mAdapter;
    TextView txtNoChildren;
    ViewFlipper viewFlipper;
    RequestQueue volleyQueue;
    FloatingActionButton fab;
    BottomSheetDialog mBottomSheetDialog;
    View sheetView;
    LinearLayout optionEdit, optionDelete;
    long selectedChildId = -1;
    long userId = -1;
    boolean init;
    String mode;
    private int PAGE_LOADING = 0;
    private int PAGE_ERROR = 1;
    private int PAGE_NO_CHILDREN = 2;
    private int PAGE_LIST = 3;
    SharedPreferences sharedPreferences;
    StepperLayout stepperLayout;
    boolean canProceed = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.manage_children_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_child:
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
//                        fab.hide();
                        mChildren.clear();


                        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                                getString(R.string.base_url) + "ChildrenAdd.php"
                                        + "?user_id=" + userId
                                        + "&child_name=" + txtChildName.getText().toString()
                                        + "&child_phone=" + txtChildPhone.getText().toString(),
                                null,
                                ManageChildrenFragment.this,
                                ManageChildrenFragment.this);
                        volleyQueue.add(request);

                    }
                });

                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mChildren = new ArrayList<>();
        sharedPreferences = getActivity().getApplicationContext().getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
        userId = sharedPreferences.getLong("user_id", -1);

        fab = view.findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//
//            }
//        });

        viewFlipper = view.findViewById(R.id.manage_children_flipper);
        volleyQueue = Volley.newRequestQueue(getContext());

        stepperLayout = view.findViewById(R.id.stepperLayout);


//        mChildren.add(new Child(0, "ali", "123"));
//        mChildren.add(new Child(1, "ahmed", "456"));
//        mChildren.add(new Child(2, "mohammad", "789"));


        mAdapter = new ChildrenListAdapter(mChildren);
        mListView = view.findViewById(R.id.listChildren);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();


        makeRequest();


//        if (!init) {
//            CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            lp.gravity = Gravity.BOTTOM | Gravity.END;
//            lp.setMargins(32, 32, 32, 32);
//            fab.setLayoutParams(lp);
//        }


        mListView.setOnItemLongClickListener(this);

        loadOptionsMenu();

        Button btnRetry = view.findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeRequest();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.content_manage_children, container, false);
    }

    private void loadOptionsMenu() {
        mBottomSheetDialog = new BottomSheetDialog(getContext());
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
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want to delete this child?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewFlipper.setDisplayedChild(PAGE_LOADING);
//                        fab.hide();
                        mChildren.clear();
                        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                                getString(R.string.base_url) + "ChildrenDelete.php"
                                        + "?user_id=" + userId
                                        + "&child_id=" + selectedChildId,
                                null,
                                ManageChildrenFragment.this,
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
//        fab.hide();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getString(R.string.base_url) + "ChildrenGet.php?user_id=" + userId, null, this, this);
        volleyQueue.add(request);

    }

    @Override
    public void onResponse(JSONArray response) {
        mChildren.clear();
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
//            fab.show();


        } catch (JSONException e) {
            e.printStackTrace();
            showError();
        }
    }

    private void showError() {
        viewFlipper.setDisplayedChild(PAGE_ERROR);
//        fab.hide();
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
    public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
        selectedChildId = id;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirmation");
        builder.setMessage("This phone/watch will be specified for your child: " + "\n"
                + mChildren.get((int) position).getChildName()
                + "\n" + "This option cannot be undone.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPreferences = getContext().getApplicationContext().getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("child_id", (int) id);
                editor.putBoolean("init", false);
                editor.apply();
                canProceed = true;
                ((StepperActivity) getActivity()).mStepperLayout.proceed();
                Intent intent = new Intent(getContext(), ChildActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        selectedChildId = id;
        mBottomSheetDialog.show();
        return true;
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {

    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        if (sharedPreferences.getString("mode", "undefined").equals("parent")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("init", false);
            editor.apply();
            canProceed = true;
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            startActivity(intent);
        } else {
            showSnackbar("Please select a child");
        }
        if (canProceed) {
            callback.complete();
        }
    }

    private void showSnackbar(String text) {
        Snackbar.make(getView(), text, Snackbar.LENGTH_LONG)
                .setAction(null, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        callback.goToPrevStep();
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {
        init = sharedPreferences.getBoolean("init", true);
        mode = sharedPreferences.getString("mode", "undefined");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(mode.equals("child") ? "Choose a Child" : "Manage Your Children");
        mListView.setOnItemClickListener(mode.equals("child") ? this : null);
    }

    @Override
    public void onError(@NonNull VerificationError error) {

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
            Glide
                    .with(getContext())
                    .load(getChildImageUrl(items.get(position).getChildId()))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(imgChild);

            return view;
        }

        private String getChildImageUrl(long childId) {
            return getString(R.string.base_url)
                    + getString(R.string.ulr_child_image_get)
                    + "?child_id=" + childId;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            if (mChildren.size() <= 0) {
                viewFlipper.setDisplayedChild(PAGE_NO_CHILDREN);
//                fab.show();
            } else {
                viewFlipper.setDisplayedChild(PAGE_LIST);
//                fab.show();
            }
        }
    }

}
